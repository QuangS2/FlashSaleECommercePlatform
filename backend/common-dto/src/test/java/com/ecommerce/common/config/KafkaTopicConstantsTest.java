package com.ecommerce.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KafkaTopicConstantsTest {

    @Test
    @DisplayName("Test 1: Order topics configuration")
    void testOrderTopics() {
        assertEquals("order-events", KafkaTopicConstants.TOPIC_ORDER_EVENTS);
    }

    @Test
    @DisplayName("Test 2: Inventory topics configuration")
    void testInventoryTopics() {
        assertEquals("inventory-events", KafkaTopicConstants.TOPIC_INVENTORY_EVENTS);
    }

    @Test
    @DisplayName("Test 3: Payment topics configuration")
    void testPaymentTopics() {
        assertEquals("payment-events", KafkaTopicConstants.TOPIC_PAYMENT_EVENTS);
    }

    @Test
    @DisplayName("Test 4: Notification topics configuration")
    void testNotificationTopics() {
        assertEquals("notification-events", KafkaTopicConstants.TOPIC_NOTIFICATION_EVENTS);
    }

    @Test
    @DisplayName("Test 5: Consumer groups naming conventions")
    void testConsumerGroups() {
        assertEquals("inventory-service-group", KafkaTopicConstants.INVENTORY_SERVICE_GROUP);
        assertEquals("payment-service-group", KafkaTopicConstants.PAYMENT_SERVICE_GROUP);
        assertEquals("order-service-group", KafkaTopicConstants.ORDER_SERVICE_GROUP);
        assertEquals("notification-service-group", KafkaTopicConstants.NOTIFICATION_SERVICE_GROUP);
    }
}
