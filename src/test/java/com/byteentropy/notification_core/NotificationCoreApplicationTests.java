package com.byteentropy.notification_core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class NotificationCoreApplicationTests {

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        // Verifies the application context starts with Virtual Threads, Redis Mocks, and JPA config
    }
}