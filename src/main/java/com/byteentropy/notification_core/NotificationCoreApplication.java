package com.byteentropy.notification_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NotificationCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationCoreApplication.class, args);
    }
}