package com.ecommerce.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockBroadcastMessage {

    private String productId;
    private Integer remainingStock;
    private String status;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static StockBroadcastMessage of(String productId, Integer remainingStock, String status) {
        return StockBroadcastMessage.builder()
                .productId(productId)
                .remainingStock(remainingStock)
                .status(status)
                .timestamp(Instant.now())
                .build();
    }
}
