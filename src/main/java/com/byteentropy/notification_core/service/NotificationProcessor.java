package com.byteentropy.notification_core.service;

import com.byteentropy.notification_core.domain.FailedNotification;
import com.byteentropy.notification_core.domain.NotificationRequest;
import com.byteentropy.notification_core.repository.FailedNotificationRepository;
import com.byteentropy.notification_core.stratergy.DeliveryStrategy;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class NotificationProcessor {
    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);
    private final List<DeliveryStrategy> deliveryStrategies;
    private final FailedNotificationRepository failedRepo;

    public NotificationProcessor(List<DeliveryStrategy> deliveryStrategies, FailedNotificationRepository failedRepo) {
        this.deliveryStrategies = deliveryStrategies;
        this.failedRepo = failedRepo;
    }

    @Retry(name = "notificationRetry", fallbackMethod = "recoverNotification")
    public void execute(NotificationRequest request) {
        DeliveryStrategy strategy = deliveryStrategies.stream()
                .filter(s -> s.getChannel() == request.channel())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No strategy for " + request.channel()));

        strategy.send(request);
    }

    /**
     * FALLBACK: Triggered after all retries fail.
     * Arguments MUST follow the original method signature + the Exception.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recoverNotification(NotificationRequest request, Throwable e) { // <--- SWAPPED & Throwable used
        // 1. Critical: Log immediately
        log.error("CRITICAL FAILURE: Notification for user {} via {} failed after retries. Error: {}", 
                  request.userId(), request.channel(), e.getMessage());

        try {
            // 2. Attempt to persist to DLQ table
            FailedNotification failed = new FailedNotification();
            failed.setUserId(request.userId());
            failed.setChannel(request.channel().name());
            failed.setTemplateId(request.templateId());
            failed.setErrorMessage(e.getMessage());
            
            failedRepo.saveAndFlush(failed);
            log.info("Successfully persisted failed notification to DLQ for user: {}", request.userId());
        } catch (Exception dbEx) {
            log.error("DLQ Persistence failed! Database unavailable. Manual recovery required: {}", request);
        }
    }
}