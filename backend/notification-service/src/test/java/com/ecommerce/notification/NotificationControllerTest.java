package com.ecommerce.notification;

import com.ecommerce.notification.controller.NotificationController;
import com.ecommerce.notification.dto.NotificationMessage;
import com.ecommerce.notification.dto.SendNotificationRequest;
import com.ecommerce.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test 1: POST /api/v1/notifications/send - Sends notification successfully")
    public void testSendNotification() throws Exception {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId("user_1001")
                .orderId("ORD-999")
                .type("ORDER_CONFIRMED")
                .title("Đơn hàng thành công")
                .content("Cảm ơn bạn đã mua hàng")
                .build();

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user_1001"))
                .andExpect(jsonPath("$.title").value("Đơn hàng thành công"));

        verify(notificationService).sendNotificationToUser(eq("user_1001"), any(NotificationMessage.class));
    }

    @Test
    @DisplayName("Test 2: GET /api/v1/notifications/user/{userId} - Returns user notifications")
    public void testGetUserNotifications() throws Exception {
        NotificationMessage notif = NotificationMessage.of(
                "user_1001",
                "ORD-999",
                "PAYMENT_SUCCESS",
                "Thanh toán thành công",
                "Đã nhận 100k"
        );
        when(notificationService.getUserNotifications("user_1001")).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/v1/notifications/user/user_1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user_1001"))
                .andExpect(jsonPath("$[0].title").value("Thanh toán thành công"));
    }

    @Test
    @DisplayName("Test 3: GET /api/v1/notifications/status - Returns UP status")
    public void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("notification-service"));
    }
}
