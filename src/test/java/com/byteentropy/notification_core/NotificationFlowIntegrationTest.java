package com.byteentropy.notification_core;

import com.byteentropy.notification_core.domain.Channel;
import com.byteentropy.notification_core.domain.NotificationRequest;
import com.byteentropy.notification_core.repository.FailedNotificationRepository;
import com.byteentropy.notification_core.stratergy.EmailDelivery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FailedNotificationRepository failedNotificationRepository;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private EmailDelivery emailDelivery;

    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        failedNotificationRepository.deleteAll();
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(emailDelivery.getChannel()).thenReturn(Channel.EMAIL);
    }

    @Test
    @DisplayName("Should block duplicate requests with 409 Conflict")
    void testIdempotency_DuplicateBlocked() throws Exception {
        NotificationRequest request = new NotificationRequest("user123", Channel.EMAIL, "tpl_1", Map.of());
        
        // Simulate Redis already has the key (Duplicate)
        when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(false);

        mockMvc.perform(post("/v1/notifications")
                .header("Idempotency-Key", "fixed-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        // Ensure the processing logic was never even touched
        verify(emailDelivery, never()).send(any());
    }

    @Test
    @DisplayName("Should allow request when Redis is down (Fail-Open)")
    void testFailOpen_WhenRedisIsDown() throws Exception {
        NotificationRequest request = new NotificationRequest("u_fail_open", Channel.EMAIL, "t1", Map.of());

        when(valueOperations.setIfAbsent(anyString(), anyString(), any()))
               .thenThrow(new RuntimeException("Redis Down"));

        mockMvc.perform(post("/v1/notifications")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(emailDelivery, atLeastOnce()).send(any(NotificationRequest.class));
        });
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid")
    void testValidation_InvalidRequest() throws Exception {
        // Missing userId and channel
        String invalidJson = "{\"templateId\":\"\"}"; 

        mockMvc.perform(post("/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should retry 3 times and then persist to DLQ on permanent failure")
    void testRetryAndDLQ_EndToEnd() throws Exception {
        NotificationRequest request = new NotificationRequest("dlq_user", Channel.EMAIL, "t1", Map.of());

        when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        
        // Force failure
        doThrow(new RuntimeException("SMTP Server Down"))
                .when(emailDelivery).send(any(NotificationRequest.class));

        mockMvc.perform(post("/v1/notifications")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // Verify Resilience4j hit the max-attempts (3)
            verify(emailDelivery, times(3)).send(any(NotificationRequest.class));
            // Verify fallback saved to Database
            assertTrue(failedNotificationRepository.count() > 0);
        });
    }
}