package com.ecommerce.payment.infrastructure.inbound.rest;

import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.payment.application.port.in.PaymentUseCase;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.dto.ProcessPaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentUseCase paymentUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testProcessPayment() throws Exception {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId("order_1")
                .userId("user_1")
                .amount(BigDecimal.valueOf(1000))
                .paymentMethod("VNPAY")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .paymentId("PAY-12345")
                .orderId("order_1")
                .status(PaymentStatus.SUCCESS)
                .transactionRef("TXN-67890")
                .build();

        when(paymentUseCase.processPayment(any(ProcessPaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-12345"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testGetPaymentByOrderId() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .paymentId("PAY-12345")
                .orderId("order_1")
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentUseCase.getPaymentByOrderId("order_1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/order/order_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order_1"));
    }

    @Test
    void testGetPaymentByPaymentId() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .paymentId("PAY-12345")
                .orderId("order_1")
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentUseCase.getPaymentByPaymentId("PAY-12345")).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/PAY-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-12345"));
    }

    @Test
    void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/v1/payments/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("payment-service"));
    }
}
