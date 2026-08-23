package com.ecommerce.common.event.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCancelledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private String reason;

    @Builder.Default
    private OrderStatus status = OrderStatus.CANCELLED;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant cancelledAt = Instant.now();
}
