package com.ecommerce.notification.controller;

import com.ecommerce.notification.dto.NotificationMessage;
import com.ecommerce.notification.dto.SendNotificationRequest;
import com.ecommerce.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Gửi thông báo tùy chỉnh qua WebSocket (Manual Push / Admin Alert).
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationMessage> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        NotificationMessage message = NotificationMessage.of(
                request.getUserId(),
                request.getOrderId(),
                request.getType(),
                request.getTitle(),
                request.getContent()
        );
        notificationService.sendNotificationToUser(request.getUserId(), message);
        return ResponseEntity.ok(message);
    }

    /**
     * Tra cứu danh sách lịch sử thông báo gần nhất của người dùng.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationMessage>> getUserNotifications(@PathVariable String userId) {
        List<NotificationMessage> list = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Endpoint kiểm tra sức khỏe service.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notification-service",
                "protocol", "WebSocket STOMP + Kafka Multi-Topic Consumer",
                "endpoints", List.of("/ws", "/ws-notification")
        ));
    }
}
