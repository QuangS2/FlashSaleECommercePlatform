package com.ecommerce.common.config;

import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.order.OrderCreatedEvent;
import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.common.kafka.EventPublisherService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerConsumerConfigTest {

    @Mock
    private KafkaTemplate<String, Object> mockKafkaTemplate;

    @Test
    @DisplayName("Test 1: Verify Producer Factory Configuration Properties (Idempotence, Acks, Snappy)")
    public void testProducerConfigs() {
        CommonKafkaProducerConfig producerConfig = new CommonKafkaProducerConfig();
        Map<String, Object> props = producerConfig.producerConfigs();

        assertThat(props.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
        assertThat(props.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(props.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
        assertThat(props.get(ProducerConfig.COMPRESSION_TYPE_CONFIG)).isEqualTo("snappy");
        assertThat(props.get(ProducerConfig.BATCH_SIZE_CONFIG)).isEqualTo(32768);
        assertThat(props.get(ProducerConfig.LINGER_MS_CONFIG)).isEqualTo(5);
        assertThat(props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class);
    }

    @Test
    @DisplayName("Test 2: Verify Consumer Factory Configuration Properties (ErrorHandlingDeserializer, No AutoCommit)")
    public void testConsumerConfigs() {
        CommonKafkaConsumerConfig consumerConfig = new CommonKafkaConsumerConfig();
        Map<String, Object> props = consumerConfig.consumerConfigs();

        assertThat(props.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo(false);
        assertThat(props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)).isEqualTo("earliest");
        assertThat(props.get(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS)).isEqualTo(StringDeserializer.class.getName());
    }

    @Test
    @DisplayName("Test 3: Verify ConcurrentKafkaListenerContainerFactory has Manual Immediate AckMode")
    public void testContainerFactoryAckMode() {
        CommonKafkaConsumerConfig consumerConfig = new CommonKafkaConsumerConfig();
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                consumerConfig.kafkaListenerContainerFactory();

        assertThat(factory).isNotNull();
        assertThat(factory.getContainerProperties().getAckMode())
                .isEqualTo(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    }

    @Test
    @DisplayName("Test 4: Verify EventPublisherService successfully delegates send to KafkaTemplate")
    public void testEventPublisherService() {
        EventPublisherService publisherService = new EventPublisherService(mockKafkaTemplate);

        OrderCreatedEvent payload = OrderCreatedEvent.builder()
                .orderId("ORD-TEST-99")
                .userId("usr-1")
                .productId("PROD-1")
                .quantity(1)
                .unitPrice(new BigDecimal("100000"))
                .totalAmount(new BigDecimal("100000"))
                .status(OrderStatus.PENDING)
                .build();

        BaseEvent<OrderCreatedEvent> event = BaseEvent.of(
                EventType.ORDER_CREATED,
                "CORR-ORD-TEST-99",
                "order-service",
                payload
        );

        CompletableFuture<SendResult<String, Object>> mockFuture = new CompletableFuture<>();
        when(mockKafkaTemplate.send(eq("order-events"), eq("CORR-ORD-TEST-99"), any(BaseEvent.class)))
                .thenReturn(mockFuture);

        CompletableFuture<SendResult<String, Object>> resultFuture =
                publisherService.publish("order-events", event);

        assertThat(resultFuture).isNotNull();
        verify(mockKafkaTemplate).send("order-events", "CORR-ORD-TEST-99", event);
    }
}
