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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void testGetOrdersByEmailParamOnRoot() throws Exception {
        when(orderUseCase.getOrdersByUserEmail("u@e.com")).thenReturn(List.of(mockResponse));
        mockMvc.perform(get("/api/v1/orders").param("email", "u@e.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("ORD-123"));
    }

    @Test
    void testGetOrdersByUserIdParamOnRoot() throws Exception {
        when(orderUseCase.getOrdersByUserId("user_1")).thenReturn(List.of(mockResponse));
        mockMvc.perform(get("/api/v1/orders").param("userId", "user_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("ORD-123"));
    }

    @Test
    void testGetOrdersByEmailEndpoints() throws Exception {
        when(orderUseCase.getOrdersByUserEmail("u@e.com")).thenReturn(List.of(mockResponse));
        mockMvc.perform(get("/api/v1/orders/email").param("email", "u@e.com"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/orders/by-email").param("email", "u@e.com"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/orders/email/u@e.com"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOrderDetailNotFound() throws Exception {
        when(orderUseCase.getOrderByOrderId("ORD-NOTFOUND")).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/api/v1/orders/{orderId}", "ORD-NOTFOUND"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/orders/detail/{orderId}", "ORD-NOTFOUND"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOrderDetailSuccess() throws Exception {
        when(orderUseCase.getOrderByOrderId("ORD-123")).thenReturn(mockResponse);
        mockMvc.perform(get("/api/v1/orders/detail/{orderId}", "ORD-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123"));
    }

    @Test
    void testCreateFlashSaleOrder_Success() throws Exception {
        com.ecommerce.order.dto.FlashSaleOrderRequest request = com.ecommerce.order.dto.FlashSaleOrderRequest.builder()
                .saleId(1L)
                .itemId(101L)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        when(orderUseCase.createFlashSaleOrder(any(), anyString())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/orders/flash-sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user_1")
                        .header("Idempotency-Key", "idemp-test")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value("ORD-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testCreateFlashSaleOrder_ConflictOutOfStock() throws Exception {
        com.ecommerce.order.dto.FlashSaleOrderRequest request = com.ecommerce.order.dto.FlashSaleOrderRequest.builder()
                .saleId(1L)
                .itemId(101L)
                .quantity(1)
                .build();

        when(orderUseCase.createFlashSaleOrder(any(), anyString()))
                .thenThrow(new com.ecommerce.order.domain.exception.StockReservationException("Hết hàng"));

        mockMvc.perform(post("/api/v1/orders/flash-sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Hết hàng"));
    }

    @Test
    void testCreateFlashSaleOrder_AnonymousAndNoHeaders() throws Exception {
        com.ecommerce.order.dto.FlashSaleOrderRequest request = com.ecommerce.order.dto.FlashSaleOrderRequest.builder()
                .saleId(1L)
                .itemId(101L)
                .quantity(1)
                .build();

        when(orderUseCase.createFlashSaleOrder(any(), eq("anonymous-user"))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/orders/flash-sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void testCreateFlashSaleOrder_BlankHeaders() throws Exception {
        com.ecommerce.order.dto.FlashSaleOrderRequest request = com.ecommerce.order.dto.FlashSaleOrderRequest.builder()
                .saleId(1L)
                .itemId(101L)
                .quantity(1)
                .build();

        when(orderUseCase.createFlashSaleOrder(any(), eq("anonymous-user"))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/orders/flash-sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "")
                        .header("Idempotency-Key", "")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }
}
