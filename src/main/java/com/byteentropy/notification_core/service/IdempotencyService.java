package com.byteentropy.notification_core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class IdempotencyService {
    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "idempotency:";

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isDuplicate(String key) {
        if (key == null || key.isBlank()) {
            return false; 
        }
        
        String redisKey = KEY_PREFIX + key;
        try {
            // SETNX: Sets the key only if it doesn't exist
            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "processed", Duration.ofHours(24));
            
            // If success is FALSE, it means the key already existed (it's a duplicate)
            return Boolean.FALSE.equals(success);
            
        } catch (Exception e) {
            // FAIL-OPEN: Log the error but allow the request to proceed
            log.error("CRITICAL: Redis unreachable. Idempotency compromised, allowing request. Error: {}", e.getMessage());
            return false; 
        }
    }
}