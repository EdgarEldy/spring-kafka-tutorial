package edgareldy.springkafkatutorial.messaging.producer;

import static org.assertj.core.api.Assertions.assertThat;

import edgareldy.springkafkatutorial.dto.event.OrderCreatedEvent;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Fast, in-memory test for {@link OrderEventProducer} against an embedded
 * Kafka broker, no Spring Boot application context involved: verifies that
 * {@link OrderEventProducer#publish(OrderCreatedEvent)}, called outside a
 * transaction, sends immediately with {@code productId} as the record key.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = OrderEventProducer.ORDER_EVENTS_TOPIC)
class OrderEventProducerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private OrderEventProducer orderEventProducer;
    private KafkaMessageListenerContainer<String, OrderCreatedEvent> container;
    private BlockingQueue<ConsumerRecord<String, OrderCreatedEvent>> records;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, OrderCreatedEvent> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        orderEventProducer = new OrderEventProducer(new KafkaTemplate<>(producerFactory));

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("order-event-producer-test", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "edgareldy.springkafkatutorial.dto.event");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreatedEvent.class.getName());
        consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        DefaultKafkaConsumerFactory<String, OrderCreatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(OrderEventProducer.ORDER_EVENTS_TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, OrderCreatedEvent>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterEach
    void tearDown() {
        container.stop();
    }

    @Test
    void publishSendsEventKeyedByProductId() throws InterruptedException {
        OrderCreatedEvent event = new OrderCreatedEvent(1L, 42L, 3);

        orderEventProducer.publish(event);

        ConsumerRecord<String, OrderCreatedEvent> received = records.poll(10, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo("42");
        assertThat(received.value()).isEqualTo(event);
    }
}
