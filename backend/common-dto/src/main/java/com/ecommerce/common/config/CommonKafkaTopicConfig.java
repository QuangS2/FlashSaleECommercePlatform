package com.ecommerce.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class CommonKafkaTopicConfig {

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(KafkaTopicConstants.TOPIC_ORDER_EVENTS)
                .partitions(KafkaTopicConstants.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.DEFAULT_REPLICATION_FACTOR)
                .config(KafkaTopicConstants.RETENTION_MS_CONFIG, KafkaTopicConstants.RETENTION_7_DAYS)
                .build();
    }

    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS)
                .partitions(KafkaTopicConstants.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.DEFAULT_REPLICATION_FACTOR)
                .config(KafkaTopicConstants.RETENTION_MS_CONFIG, KafkaTopicConstants.RETENTION_7_DAYS)
                .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS)
                .partitions(KafkaTopicConstants.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.DEFAULT_REPLICATION_FACTOR)
                .config(KafkaTopicConstants.RETENTION_MS_CONFIG, KafkaTopicConstants.RETENTION_7_DAYS)
                .build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(KafkaTopicConstants.TOPIC_NOTIFICATION_EVENTS)
                .partitions(KafkaTopicConstants.DEFAULT_PARTITIONS)
                .replicas(KafkaTopicConstants.DEFAULT_REPLICATION_FACTOR)
                .config(KafkaTopicConstants.RETENTION_MS_CONFIG, KafkaTopicConstants.RETENTION_1_DAY)
                .build();
    }
}
