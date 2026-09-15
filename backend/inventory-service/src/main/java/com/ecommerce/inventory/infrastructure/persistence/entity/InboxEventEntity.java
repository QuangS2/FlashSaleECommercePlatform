package com.ecommerce.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "inbox_events", indexes = {
        @Index(name = "idx_inbox_consumer_group", columnList = "consumer_group")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboxEventEntity {

    @Id
    @Column(name = "message_id", nullable = false, length = 64)
    private String messageId;

    @Column(name = "consumer_group", nullable = false, length = 64)
    private String consumerGroup;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @PrePersist
    protected void onCreate() {
        if (this.processedAt == null) {
            this.processedAt = Instant.now();
        }
    }
}
