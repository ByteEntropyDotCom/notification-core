package com.byteentropy.notification_core.service;

import com.byteentropy.notification_core.domain.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationProcessor processor;

    public NotificationService(NotificationProcessor processor) {
        this.processor = processor;
    }

    @Async
    public void processNotification(NotificationRequest request) {
        log.info("Starting Async processing for user: {}", request.userId());
        // This call now goes through the Processor's Retry Proxy
        processor.execute(request);
    }
}