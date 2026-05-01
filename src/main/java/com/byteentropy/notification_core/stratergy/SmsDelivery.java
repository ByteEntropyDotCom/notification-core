package com.byteentropy.notification_core.stratergy;

import com.byteentropy.notification_core.domain.Channel;
import com.byteentropy.notification_core.domain.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class SmsDelivery implements DeliveryStrategy {
    private static final Logger log = LoggerFactory.getLogger(SmsDelivery.class);

    @Override
    public void send(NotificationRequest request) {
        log.info("Dispatching SMS to user {}: Payload size {}", 
                 request.userId(), 
                 request.payload() != null ? request.payload().size() : 0);
        
        // Virtual Threads friendly delay
        // In production, this would be a RestClient or WebClient call
        try {
            Thread.sleep(Duration.ofMillis(50)); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }
}