package com.ecommerce.payment;

import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.payment.controller.PaymentController;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.dto.ProcessPaymentRequest;
import com.ecommerce.payment.service.PaymentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    @DisplayName("Test 1: POST /api/v1/payments/process - Processes payment and returns 200 OK")
    public void testProcessPayment_Success() throws Exception {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId("ORD-999")
                .userId("user_1001")
                .amount(new BigDecimal("29990000"))
                .paymentMethod("VNPAY")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .paymentId("PAY-999")
                .orderId("ORD-999")
                .amount(new BigDecimal("29990000"))
                .paymentMethod("VNPAY")
                .status(PaymentStatus.SUCCESS)
                .transactionRef("TXN-12345")
                .paidAt(Instant.now())
                .message("Thanh toán thành công qua VNPAY")
                .build();

        when(paymentService.processPayment(any(ProcessPaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-999"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(29990000));
    }

    @Test
    @DisplayName("Test 2: GET /api/v1/payments/order/{orderId} - Returns payment response")
    public void testGetPaymentByOrderId() throws Exception {
        String orderId = "ORD-999";
        PaymentResponse response = PaymentResponse.builder()
                .paymentId("PAY-999")
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/order/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-999"))
                .andExpect(jsonPath("$.orderId").value(orderId));
    }

    @Test
    @DisplayName("Test 3: GET /api/v1/payments/{paymentId} - Returns payment response")
    public void testGetPaymentByPaymentId() throws Exception {
        String paymentId = "PAY-999";
        PaymentResponse response = PaymentResponse.builder()
                .paymentId(paymentId)
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/" + paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId));
    }

    @Test
    @DisplayName("Test 4: GET /api/v1/payments/status - Returns Service Health Status UP")
    public void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/v1/payments/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("payment-service"));
    }
}
