package edgareldy.springkafkatutorial.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import edgareldy.springkafkatutorial.dto.event.OrderCreatedEvent;
import edgareldy.springkafkatutorial.entity.Category;
import edgareldy.springkafkatutorial.entity.Product;
import edgareldy.springkafkatutorial.messaging.producer.OrderEventProducer;
import edgareldy.springkafkatutorial.repository.CategoryRepository;
import edgareldy.springkafkatutorial.repository.ProductRepository;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;

/**
 * End-to-end messaging tests against a real Kafka broker and a real
 * PostgreSQL database, both via Testcontainers (see
 * {@link MessagingTestcontainersConfiguration}): closer to production than the
 * Mockito/EmbeddedKafka unit tests elsewhere, since the full
 * {@code KafkaProducerConfig}/{@code KafkaConsumerConfig} wiring,
 * {@code OrderEventConsumer}, {@code StockService}, and the
 * retry/dead-letter pipeline all run for real. Order creation itself is
 * bypassed here: {@link OrderEventProducer} is called directly with a
 * hand-built {@link OrderCreatedEvent}, since {@code StockService} only
 * needs a {@code productId}, not a real {@code Order} row.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@SpringBootTest(properties = {
        "app.kafka.retry.max-attempts=2",
        "app.kafka.retry.initial-interval-ms=10",
        "app.kafka.retry.multiplier=1.0",
        "app.kafka.retry.max-interval-ms=20"
})
@Import(MessagingTestcontainersConfiguration.class)
class OrderMessagingIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderEventProducer orderEventProducer;

    @Autowired
    private KafkaConnectionDetails kafkaConnectionDetails;

    private KafkaMessageListenerContainer<String, OrderCreatedEvent> dltContainer;

    @AfterEach
    void tearDown() {
        if (dltContainer != null) {
            dltContainer.stop();
        }
    }

    @Test
    void publishingOrderCreatedEventEventuallyDecrementsStock() {
        Category category = categoryRepository.save(Category.builder().categoryName("Electronics").build());
        Product product = productRepository.save(Product.builder()
                .category(category).productName("Keyboard").unitPrice(79.99f).stockQuantity(10).build());

        orderEventProducer.publish(new OrderCreatedEvent(1L, product.getId(), 3));

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Product refreshed = productRepository.findById(product.getId()).orElseThrow();
            assertThat(refreshed.getStockQuantity()).isEqualTo(7);
        });
    }

    @Test
    void eventThatKeepsFailingLandsOnDeadLetterTopicAndNeverDecrementsStock() throws InterruptedException {
        Category category = categoryRepository.save(Category.builder().categoryName("Electronics").build());
        Product product = productRepository.save(Product.builder()
                .category(category).productName("Out of Stock Monitor").unitPrice(249.99f).stockQuantity(0).build());
        OrderCreatedEvent event = new OrderCreatedEvent(2L, product.getId(), 5);

        BlockingQueue<ConsumerRecord<String, OrderCreatedEvent>> dltRecords = startDltListener();

        orderEventProducer.publish(event);

        ConsumerRecord<String, OrderCreatedEvent> dltRecord = dltRecords.poll(20, TimeUnit.SECONDS);

        assertThat(dltRecord).isNotNull();
        assertThat(dltRecord.value()).isEqualTo(event);
        assertThat(dltRecord.headers().lastHeader(KafkaHeaders.DLT_ORIGINAL_TOPIC).value())
                .isEqualTo(OrderEventProducer.ORDER_EVENTS_TOPIC.getBytes());

        Product refreshed = productRepository.findById(product.getId()).orElseThrow();
        assertThat(refreshed.getStockQuantity()).isZero();
    }

    private BlockingQueue<ConsumerRecord<String, OrderCreatedEvent>> startDltListener() {
        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "order-messaging-integration-test-dlt",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "edgareldy.springkafkatutorial.dto.event",
                JsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreatedEvent.class.getName(),
                JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        DefaultKafkaConsumerFactory<String, OrderCreatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(OrderEventProducer.ORDER_EVENTS_DLT_TOPIC);
        dltContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        BlockingQueue<ConsumerRecord<String, OrderCreatedEvent>> records = new LinkedBlockingQueue<>();
        dltContainer.setupMessageListener((MessageListener<String, OrderCreatedEvent>) records::add);
        dltContainer.start();
        ContainerTestUtils.waitForAssignment(dltContainer, 1);
        return records;
    }
}
