package com.ecommerce.order.infrastructure.inbound.rest;

import com.ecommerce.order.application.port.in.OrderUseCase;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderUseCase orderUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = OrderResponse.builder()
                .orderId("ORD-123")
                .userId("user_1")
                .productId("prod_1")
                .quantity(1)
                .totalAmount(new BigDecimal("100.00"))
                .status(com.ecommerce.common.event.order.OrderStatus.PENDING)
                .message("Success")
                .build();
    }

    @Test
    void testCreateOrder() throws Exception {
        when(orderUseCase.createOrder(any(CreateOrderRequest.class))).thenReturn(mockResponse);

        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user_1")
                .productId("prod_1")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value("ORD-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetOrderByOrderId() throws Exception {
        when(orderUseCase.getOrderByOrderId("ORD-123")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/orders/{orderId}", "ORD-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123"));
    }

    @Test
    void testGetOrdersByUserId() throws Exception {
        when(orderUseCase.getOrdersByUserId("user_1")).thenReturn(Arrays.asList(mockResponse));

        mockMvc.perform(get("/api/v1/orders/user/{userId}", "user_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("ORD-123"));
    }

    @Test
    void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/v1/orders/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("order-service"));
    }
}
