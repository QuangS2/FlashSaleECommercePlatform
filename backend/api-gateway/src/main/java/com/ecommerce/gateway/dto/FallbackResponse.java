package com.ecommerce.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FallbackResponse {

    private int status;
    private String error;
    private String service;
    private String message;
    private int retryAfterSeconds;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public static FallbackResponse of(int status, String error, String service, String message, int retryAfterSeconds) {
        return FallbackResponse.builder()
                .status(status)
                .error(error)
                .service(service)
                .message(message)
                .retryAfterSeconds(retryAfterSeconds)
                .timestamp(Instant.now())
                .build();
    }
}
