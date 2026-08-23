package com.ecommerce.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String status;
    private String title;
    private String content;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private Map<String, Object> metadata;

    @JsonProperty("message")
    public String getMessage() {
        return content;
    }

    public static NotificationMessage of(String userId, String orderId, String type, String title, String content) {
        String status = "INFO";
        if ("ORDER_CONFIRMED".equals(type) || "PAYMENT_SUCCESS".equals(type) || "SUCCESS".equals(type)) {
            status = "SUCCESS";
        } else if ("ORDER_CANCELLED".equals(type) || "PAYMENT_FAILED".equals(type) || "OUT_OF_STOCK".equals(type) || "ERROR".equals(type)) {
            status = "ERROR";
        }

        return NotificationMessage.builder()
                .id("NOTIF-" + System.currentTimeMillis())
                .userId(userId)
                .orderId(orderId)
                .type(type)
                .status(status)
                .title(title)
                .content(content)
                .timestamp(Instant.now())
                .build();
    }
}
