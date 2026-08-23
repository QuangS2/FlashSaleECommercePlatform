package com.ecommerce.notification;

import com.ecommerce.notification.dto.NotificationMessage;
import com.ecommerce.notification.dto.StockBroadcastMessage;
import com.ecommerce.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("Test 1: sendNotificationToUser - Pushes STOMP message and saves to user buffer")
    public void testSendNotificationToUser() {
        String userId = "user_1001";
        NotificationMessage message = NotificationMessage.of(
                userId,
                "ORD-999",
                "ORDER_CONFIRMED",
                "Đặt hàng thành công",
                "Đơn hàng đã được xác nhận"
        );

        notificationService.sendNotificationToUser(userId, message);

        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/" + userId), eq(message));
        verify(messagingTemplate).convertAndSendToUser(eq(userId), eq("/queue/notifications"), eq(message));

        List<NotificationMessage> userHistory = notificationService.getUserNotifications(userId);
        assertThat(userHistory).hasSize(1);
        assertThat(userHistory.get(0).getTitle()).isEqualTo("Đặt hàng thành công");
    }

    @Test
    @DisplayName("Test 2: broadcastStockUpdate - Broadcasts live stock update to topics")
    public void testBroadcastStockUpdate() {
        String productId = "PROD-FLASH-IPHONE";
        int remaining = 15;

        notificationService.broadcastStockUpdate(productId, remaining, "UPDATED");

        verify(messagingTemplate).convertAndSend(eq("/topic/flashsale-stock"), any(StockBroadcastMessage.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/flashsale/stock/" + productId), any(StockBroadcastMessage.class));
    }

    @Test
    @DisplayName("Test 3: broadcastOrderUpdate - Broadcasts order status update to /topic/orders/{orderId}")
    public void testBroadcastOrderUpdate() {
        String orderId = "ORD-TEST-888";
        NotificationMessage message = NotificationMessage.of(
                "user_1002",
                orderId,
                "ORDER_CREATED",
                "Đã nhận đơn",
                "Đang xử lý"
        );

        notificationService.broadcastOrderUpdate(orderId, message);

        verify(messagingTemplate).convertAndSend(eq("/topic/orders/" + orderId), eq(message));
    }
}
