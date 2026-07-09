package edgareldy.springkafkatutorial.messaging.consumer;

import edgareldy.springkafkatutorial.dto.event.OrderCreatedEvent;
import edgareldy.springkafkatutorial.messaging.producer.OrderEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code order-events-dlt}: the final landing spot for an
 * {@code OrderCreatedEvent} that {@code OrderEventConsumer} failed to
 * process after every retry configured in {@code KafkaConsumerConfig}.
 * This listener only logs the failure at ERROR level, the basis for a
 * future alert or a manual reprocessing tool; it never rethrows, so a
 * problem consuming the dead-letter topic itself cannot re-block the
 * original {@code order-events} partition.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@Component
@Slf4j
public class OrderEventDltConsumer {

    @KafkaListener(
            topics = OrderEventProducer.ORDER_EVENTS_DLT_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}-dlt")
    public void consume(
            OrderCreatedEvent event,
            @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
            @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage) {
        log.error("OrderCreatedEvent for order {} (product {}, quantity {}) landed on {} from {} "
                        + "after exhausting retries: {}",
                event.orderId(), event.productId(), event.quantity(),
                OrderEventProducer.ORDER_EVENTS_DLT_TOPIC, originalTopic, exceptionMessage);
    }
}
