package com.ecommerce.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEvent<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private EventType eventType;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp = Instant.now();

    private String correlationId;

    private String sourceService;

    @Builder.Default
    private String version = "1.0";

    private T payload;

    public static <T> BaseEvent<T> of(EventType eventType, String correlationId, String sourceService, T payload) {
        return BaseEvent.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .sourceService(sourceService)
                .version("1.0")
                .payload(payload)
                .build();
    }
}
