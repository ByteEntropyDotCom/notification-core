package com.byteentropy.notification_core.controller;

import com.byteentropy.notification_core.domain.NotificationRequest;
import com.byteentropy.notification_core.service.IdempotencyService;
import com.byteentropy.notification_core.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final IdempotencyService idempotencyService;

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    public NotificationController(NotificationService notificationService, 
                                  IdempotencyService idempotencyService) {
        this.notificationService = notificationService;
        this.idempotencyService = idempotencyService;
    }

@PostMapping
public ResponseEntity<String> send(
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody NotificationRequest request) {

    if (idempotencyKey != null && idempotencyService.isDuplicate(idempotencyKey)) {
        log.warn("Duplicate request blocked: {}", idempotencyKey); 
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Duplicate request: Notification with this key is already processed.");
    }

    notificationService.processNotification(request);

    return ResponseEntity.accepted()
            .body("Notification accepted and queued for delivery.");
}
}