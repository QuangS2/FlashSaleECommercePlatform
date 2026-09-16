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

    @Test
    @DisplayName("Test 4: sendNotificationToUser with null/blank userId defaults to broadcast-user")
    public void testSendNotificationWithNullOrBlankUserId() {
        NotificationMessage message = NotificationMessage.of(null, "ORD-NULL", "INFO", "Broadcast", "Global alert");

        notificationService.sendNotificationToUser(null, message);

        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/broadcast-user"), eq(message));
        List<NotificationMessage> list = notificationService.getUserNotifications("broadcast-user");
        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Test 5: getUserNotifications returns empty list for unknown user")
    public void testGetUserNotificationsEmpty() {
        List<NotificationMessage> list = notificationService.getUserNotifications("unknown_user");
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("Test 6: buffer size limit caps at MAX_BUFFER_SIZE_PER_USER (50)")
    public void testNotificationBufferCap() {
        String userId = "heavy_user";
        for (int i = 0; i < 55; i++) {
            NotificationMessage msg = NotificationMessage.of(userId, "ORD-" + i, "INFO", "Title " + i, "Body");
            notificationService.sendNotificationToUser(userId, msg);
        }

        List<NotificationMessage> list = notificationService.getUserNotifications(userId);
        assertThat(list).hasSize(50);
    }
}
