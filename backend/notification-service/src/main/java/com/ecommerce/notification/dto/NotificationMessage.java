package com.ecommerce.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {

    private String id;
    private String userId;
    private String orderId;
    private String type;
    private String title;
    private String content;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private Map<String, Object> metadata;

    public static NotificationMessage of(String userId, String orderId, String type, String title, String content) {
        return NotificationMessage.builder()
                .id("NOTIF-" + System.currentTimeMillis())
                .userId(userId)
                .orderId(orderId)
                .type(type)
                .title(title)
                .content(content)
                .timestamp(Instant.now())
                .build();
    }
}
