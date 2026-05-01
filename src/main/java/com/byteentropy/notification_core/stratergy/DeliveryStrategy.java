package com.byteentropy.notification_core.stratergy;

import com.byteentropy.notification_core.domain.Channel;
import com.byteentropy.notification_core.domain.NotificationRequest;

public interface DeliveryStrategy {
    void send(NotificationRequest request);
    Channel getChannel();
}