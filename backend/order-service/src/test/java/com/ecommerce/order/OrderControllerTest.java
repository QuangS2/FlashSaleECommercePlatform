package com.ecommerce.order;

import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.order.controller.OrderController;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    @DisplayName("Test 1: POST /api/v1/orders - Returns HTTP 202 Accepted and Order details")
    public void testCreateOrder_Success() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user_1001")
                .userEmail("quang@ecommerce.vn")
                .productId("PROD-IPHONE-15")
                .productTitle("iPhone 15 Pro Max")
                .quantity(1)
                .unitPrice(new BigDecimal("29990000"))
                .build();

        OrderResponse response = OrderResponse.builder()
                .orderId("ORD-SAMPLE-12345")
                .userId("user_1001")
                .userEmail("quang@ecommerce.vn")
                .productId("PROD-IPHONE-15")
                .productTitle("iPhone 15 Pro Max")
                .quantity(1)
                .unitPrice(new BigDecimal("29990000"))
                .totalAmount(new BigDecimal("29990000"))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .message("Đơn hàng đã được tiếp nhận thành công")
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value("ORD-SAMPLE-12345"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(29990000));
    }

    @Test
    @DisplayName("Test 2: GET /api/v1/orders/{orderId} - Returns HTTP 200 OK")
    public void testGetOrderByOrderId() throws Exception {
        String orderId = "ORD-SAMPLE-12345";
        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("29990000"))
                .build();

        when(orderService.getOrderByOrderId(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Test 3: GET /api/v1/orders/user/{userId} - Returns list of orders")
    public void testGetOrdersByUserId() throws Exception {
        String userId = "user_1001";
        OrderResponse response = OrderResponse.builder()
                .orderId("ORD-1")
                .userId(userId)
                .status(OrderStatus.CONFIRMED)
                .build();

        when(orderService.getOrdersByUserId(userId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/orders/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("ORD-1"));
    }

    @Test
    @DisplayName("Test 4: GET /api/v1/orders/status - Returns Service Health Status UP")
    public void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/v1/orders/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("order-service"));
    }
}
