package com.ecommerce.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
@RestController
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @GetMapping("/api/v1/notifications/status")
    public Map<String, Object> getStatus() {
        return Map.of("status", "UP", "service", "notification-service", "protocol", "WebSocket STOMP + Kafka");
    }
}
