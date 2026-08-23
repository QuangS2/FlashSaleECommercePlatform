package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.NotificationMessage;
import com.ecommerce.notification.dto.StockBroadcastMessage;

import java.util.List;

public interface NotificationService {

    /**
     * Gửi thông báo trực tiếp đến người dùng qua WebSocket Private Channel (/topic/notifications/{userId}).
     */
    void sendNotificationToUser(String userId, NotificationMessage message);

    /**
     * Truyền phát cập nhật tồn kho Flash Sale đến tất cả người dùng (/topic/flashsale-stock).
     */
    void broadcastStockUpdate(String productId, int remainingStock, String status);

    /**
     * Truyền phát tiến trình đơn hàng theo orderId (/topic/orders/{orderId}).
     */
    void broadcastOrderUpdate(String orderId, NotificationMessage message);

    /**
     * Lấy lịch sử thông báo của người dùng.
     */
    List<NotificationMessage> getUserNotifications(String userId);
}
