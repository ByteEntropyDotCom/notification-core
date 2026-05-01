package com.byteentropy.notification_core.stratergy;

import com.byteentropy.notification_core.domain.Channel;
import com.byteentropy.notification_core.domain.NotificationRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailDelivery implements DeliveryStrategy {
    private static final Logger log = LoggerFactory.getLogger(EmailDelivery.class);

    @Override
    public void send(NotificationRequest request) {
        // Simulate SMTP/API latency
        log.info("Sending EMAIL to user {}: Template {}", request.userId(), request.templateId());
    }

    @Override
    public Channel getChannel() {
        return Channel.EMAIL;
    }
}