package com.ecommerce.notification.service.impl;

import com.ecommerce.notification.dto.NotificationMessage;
import com.ecommerce.notification.dto.StockBroadcastMessage;
import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final SimpMessagingTemplate messagingTemplate;

    // Bộ đệm lưu trữ thông báo gần nhất của từng user (Thread-safe)
    private final Map<String, List<NotificationMessage>> userNotificationBuffer = new ConcurrentHashMap<>();
    private static final int MAX_BUFFER_SIZE_PER_USER = 50;

    @Override
    public void sendNotificationToUser(String userId, NotificationMessage message) {
        if (userId == null || userId.isBlank()) {
            userId = "broadcast-user";
        }

        log.info("[WEBSOCKET PUSH] Đẩy thông báo đến User [{}] - Loại: {} - Tiêu đề: {}",
                userId, message.getType(), message.getTitle());

        // 1. Bắn qua Topic công khai theo UserId
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, message);

        // 2. Bắn qua Private Queue theo chuẩn Spring STOMP User Destination
        try {
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);
        } catch (Exception e) {
            log.debug("[WEBSOCKET] Không thể gửi tới UserDestination riêng: {}", e.getMessage());
        }

        // 3. Lưu vào lịch sử người dùng
        userNotificationBuffer.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(0, message);

        // Giới hạn kích thước bộ đệm
        List<NotificationMessage> list = userNotificationBuffer.get(userId);
        if (list != null && list.size() > MAX_BUFFER_SIZE_PER_USER) {
            list.remove(list.size() - 1);
        }
    }

    @Override
    public void broadcastStockUpdate(String productId, int remainingStock, String status) {
        StockBroadcastMessage payload = StockBroadcastMessage.of(productId, remainingStock, status);
        log.info("[WEBSOCKET BROADCAST] Cập nhật tồn kho Flash Sale cho sản phẩm [{}]: Tồn kho còn lại = {}",
                productId, remainingStock);

        // Phát đến kênh tổng và kênh riêng của sản phẩm
        messagingTemplate.convertAndSend("/topic/flashsale-stock", payload);
        messagingTemplate.convertAndSend("/topic/flashsale/stock/" + productId, payload);
    }

    @Override
    public void broadcastOrderUpdate(String orderId, NotificationMessage message) {
        log.info("[WEBSOCKET ORDER] Cập nhật tiến độ đơn hàng [{}] qua /topic/orders/{}", orderId, orderId);
        messagingTemplate.convertAndSend("/topic/orders/" + orderId, message);
    }

    @Override
    public List<NotificationMessage> getUserNotifications(String userId) {
        return userNotificationBuffer.getOrDefault(userId, Collections.emptyList());
    }
}
