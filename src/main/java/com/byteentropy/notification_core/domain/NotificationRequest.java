package com.byteentropy.notification_core.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record NotificationRequest(
    @NotBlank String userId,
    @NotNull Channel channel,
    @NotBlank String templateId,
    Map<String, String> payload
) {}