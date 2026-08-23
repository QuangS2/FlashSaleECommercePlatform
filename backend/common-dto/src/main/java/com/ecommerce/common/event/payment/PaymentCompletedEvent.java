package com.ecommerce.common.event.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionReference;

    @Builder.Default
    private PaymentStatus status = PaymentStatus.SUCCESS;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant completedAt = Instant.now();
}
