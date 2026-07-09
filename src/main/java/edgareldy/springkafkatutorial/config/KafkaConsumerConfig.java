package edgareldy.springkafkatutorial.config;

import edgareldy.springkafkatutorial.dto.event.OrderCreatedEvent;
import edgareldy.springkafkatutorial.messaging.producer.OrderEventProducer;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/**
 * Consumer side Kafka wiring for {@link OrderEventConsumer}: a
 * {@code ConsumerFactory} wrapping key/value deserialization in
 * {@link ErrorHandlingDeserializer} (so a malformed record cannot crash the
 * listener thread, a classic "poison pill"), restricted to deserializing
 * {@link OrderCreatedEvent} from the {@code dto.event} package only, and a
 * {@code ConcurrentKafkaListenerContainerFactory} configured with a bounded
 * exponential backoff: a handful of retries with growing delay, then the
 * record is routed to {@code order-events-dlt} via
 * {@link DeadLetterPublishingRecoverer} so one poisoned record never blocks
 * the rest of its partition indefinitely.
 * <p>
 * Bootstrap servers come from {@link KafkaConnectionDetails}, not a
 * {@code @Value("${spring.kafka.bootstrap-servers}")} placeholder: see
 * {@code KafkaProducerConfig}'s Javadoc for why (a Testcontainers
 * {@code @ServiceConnection} overrides this abstraction, never the raw
 * property).
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@Configuration
public class KafkaConsumerConfig {

    private final KafkaConnectionDetails kafkaConnectionDetails;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${app.kafka.retry.max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${app.kafka.retry.initial-interval-ms:500}")
    private long retryInitialIntervalMs;

    @Value("${app.kafka.retry.multiplier:2.0}")
    private double retryMultiplier;

    @Value("${app.kafka.retry.max-interval-ms:5000}")
    private long retryMaxIntervalMs;

    public KafkaConsumerConfig(KafkaConnectionDetails kafkaConnectionDetails) {
        this.kafkaConnectionDetails = kafkaConnectionDetails;
    }

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> orderCreatedEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "edgareldy.springkafkatutorial.dto.event");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreatedEvent.class.getName());
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCreatedEvent> orderCreatedEventConsumerFactory,
            KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderCreatedEventConsumerFactory);
        factory.setCommonErrorHandler(errorHandler(orderCreatedEventKafkaTemplate));
        return factory;
    }

    private DefaultErrorHandler errorHandler(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new TopicPartition(OrderEventProducer.ORDER_EVENTS_DLT_TOPIC, -1));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(retryMaxAttempts);
        backOff.setInitialInterval(retryInitialIntervalMs);
        backOff.setMultiplier(retryMultiplier);
        backOff.setMaxInterval(retryMaxIntervalMs);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
