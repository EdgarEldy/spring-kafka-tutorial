package edgareldy.springkafkatutorial.config;

import edgareldy.springkafkatutorial.dto.event.OrderCreatedEvent;
import edgareldy.springkafkatutorial.messaging.producer.OrderEventProducer;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Producer side Kafka wiring: a strongly typed
 * {@code ProducerFactory<String, OrderCreatedEvent>}/{@code KafkaTemplate}
 * pair (rather than the generic {@code Object} template Spring Boot
 * autoconfigures) so {@link OrderEventProducer} gets compile time safety on
 * the event payload, plus the {@code NewTopic} declarations for
 * {@code order-events} and its dead-letter topic.
 * <p>
 * {@code order-events} gets 3 partitions: since a Kafka consumer group can
 * only have one active consumer per partition at a time, this is what caps
 * how many {@code OrderEventConsumer} instances in the same group can
 * process records in parallel. {@code order-events-dlt} gets a single
 * partition, since it exists for visibility into failures, not throughput.
 * <p>
 * Bootstrap servers come from {@link KafkaConnectionDetails} rather than a
 * {@code @Value("${spring.kafka.bootstrap-servers}")} placeholder: Spring
 * Boot resolves that placeholder from {@code application.yml} only, but
 * tests wire Kafka through a Testcontainers {@code @ServiceConnection},
 * which only overrides {@code KafkaConnectionDetails}, never the raw
 * property. Reading through this abstraction works in both dev/prod
 * (backed by {@code spring.kafka.*}) and tests (backed by the container).
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@Configuration
public class KafkaProducerConfig {

    private final KafkaConnectionDetails kafkaConnectionDetails;

    public KafkaProducerConfig(KafkaConnectionDetails kafkaConnectionDetails) {
        this.kafkaConnectionDetails = kafkaConnectionDetails;
    }

    @Bean
    public ProducerFactory<String, OrderCreatedEvent> orderCreatedEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate(
            ProducerFactory<String, OrderCreatedEvent> orderCreatedEventProducerFactory) {
        return new KafkaTemplate<>(orderCreatedEventProducerFactory);
    }

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(OrderEventProducer.ORDER_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderEventsDltTopic() {
        return TopicBuilder.name(OrderEventProducer.ORDER_EVENTS_DLT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
