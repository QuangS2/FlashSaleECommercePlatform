package com.ecommerce.common.event.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private String targetChannel; // e.g. "/user/queue/notifications" or "/topic/flashsale-stock"
    private Map<String, Object> metadata;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt = Instant.now();
}
