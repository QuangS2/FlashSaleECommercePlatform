package com.ecommerce.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.utils.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaTopicConfigTest {

    private final CommonKafkaTopicConfig topicConfig = new CommonKafkaTopicConfig();

    @Test
    @DisplayName("Test 1: Verify NewTopic definitions have 3 partitions and retention policies")
    public void testNewTopicDefinitions() {
        // 1. Order Events Topic
        NewTopic orderTopic = topicConfig.orderEventsTopic();
        assertThat(orderTopic.name()).isEqualTo(KafkaTopicConstants.TOPIC_ORDER_EVENTS);
        assertThat(orderTopic.numPartitions()).isEqualTo(3);
        assertThat(orderTopic.replicationFactor()).isEqualTo((short) 1);
        assertThat(orderTopic.configs().get(KafkaTopicConstants.RETENTION_MS_CONFIG))
                .isEqualTo(KafkaTopicConstants.RETENTION_7_DAYS);

        // 2. Inventory Events Topic
        NewTopic invTopic = topicConfig.inventoryEventsTopic();
        assertThat(invTopic.name()).isEqualTo(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS);
        assertThat(invTopic.numPartitions()).isEqualTo(3);
        assertThat(invTopic.replicationFactor()).isEqualTo((short) 1);

        // 3. Payment Events Topic
        NewTopic payTopic = topicConfig.paymentEventsTopic();
        assertThat(payTopic.name()).isEqualTo(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS);
        assertThat(payTopic.numPartitions()).isEqualTo(3);

        // 4. Notification Events Topic
        NewTopic notifTopic = topicConfig.notificationEventsTopic();
        assertThat(notifTopic.name()).isEqualTo(KafkaTopicConstants.TOPIC_NOTIFICATION_EVENTS);
        assertThat(notifTopic.numPartitions()).isEqualTo(3);
        assertThat(notifTopic.configs().get(KafkaTopicConstants.RETENTION_MS_CONFIG))
                .isEqualTo(KafkaTopicConstants.RETENTION_1_DAY);
    }

    @Test
    @DisplayName("Test 2: Verify Partitioning Strategy (Deterministic Murmur2 Hash per OrderId)")
    public void testMurmur2PartitionRouting() {
        String testOrderId = "ORD-FLASH-SALE-998811";
        byte[] keyBytes = testOrderId.getBytes(StandardCharsets.UTF_8);

        // Thuật toán Murmur2 chuẩn của Apache Kafka: Utils.toPositive(Utils.murmur2(bytes)) % numPartitions
        int expectedPartition = Utils.toPositive(Utils.murmur2(keyBytes)) % KafkaTopicConstants.DEFAULT_PARTITIONS;
        System.out.println("[TEST LOG] OrderId: " + testOrderId + " -> Định tuyến vào Kafka Partition: " + expectedPartition);

        // Mô phỏng 10 sự kiện liên tiếp của cùng 1 đơn hàng qua các topic khác nhau
        for (int i = 0; i < 10; i++) {
            int partition = Utils.toPositive(Utils.murmur2(keyBytes)) % KafkaTopicConstants.DEFAULT_PARTITIONS;
            assertThat(partition).isEqualTo(expectedPartition);
        }
    }

    @Test
    @DisplayName("Test 3: Verify Even Distribution across 3 Partitions for multiple Order IDs")
    public void testPartitionDistribution() {
        Map<Integer, Integer> partitionHistogram = new HashMap<>();
        partitionHistogram.put(0, 0);
        partitionHistogram.put(1, 0);
        partitionHistogram.put(2, 0);

        // Mô phỏng 300 đơn hàng Flash Sale ngẫu nhiên
        for (int i = 0; i < 300; i++) {
            String orderId = "ORD-" + i;
            byte[] keyBytes = orderId.getBytes(StandardCharsets.UTF_8);
            int partition = Utils.toPositive(Utils.murmur2(keyBytes)) % KafkaTopicConstants.DEFAULT_PARTITIONS;
            partitionHistogram.put(partition, partitionHistogram.get(partition) + 1);
        }

        System.out.println("[TEST LOG] Phân bổ 300 đơn hàng vào 3 Partitions: " + partitionHistogram);

        // Đảm bảo tất cả 3 partitions đều tiếp nhận dữ liệu (tải được cân bằng đều)
        assertThat(partitionHistogram.get(0)).isGreaterThan(50);
        assertThat(partitionHistogram.get(1)).isGreaterThan(50);
        assertThat(partitionHistogram.get(2)).isGreaterThan(50);
    }
}
