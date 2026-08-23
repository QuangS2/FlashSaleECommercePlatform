package com.ecommerce.common.config;

public final class KafkaTopicConstants {

    private KafkaTopicConstants() {
        // Prevent instantiation
    }

    // =========================================================================
    // 1. Topic Names
    // =========================================================================
    public static final String TOPIC_ORDER_EVENTS = "order-events";
    public static final String TOPIC_INVENTORY_EVENTS = "inventory-events";
    public static final String TOPIC_PAYMENT_EVENTS = "payment-events";
    public static final String TOPIC_NOTIFICATION_EVENTS = "notification-events";

    // =========================================================================
    // 2. Partition & Replication Topologies
    // =========================================================================
    public static final int DEFAULT_PARTITIONS = 3;
    public static final short DEFAULT_REPLICATION_FACTOR = 1;

    // =========================================================================
    // 3. Consumer Group Identifiers
    // =========================================================================
    public static final String ORDER_SERVICE_GROUP = "order-service-group";
    public static final String INVENTORY_SERVICE_GROUP = "inventory-service-group";
    public static final String PAYMENT_SERVICE_GROUP = "payment-service-group";
    public static final String NOTIFICATION_SERVICE_GROUP = "notification-service-group";

    // =========================================================================
    // 4. Topic Configurations & Retention Policies (ms)
    // =========================================================================
    public static final String RETENTION_MS_CONFIG = "retention.ms";
    public static final String RETENTION_7_DAYS = "604800000";   // 7 days for financial/transactional events
    public static final String RETENTION_1_DAY = "86400000";     // 1 day for ephemeral notifications
}
