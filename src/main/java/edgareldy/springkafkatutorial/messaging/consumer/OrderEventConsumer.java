package edgareldy.springkafkatutorial.messaging.consumer;

import edgareldy.springkafkatutorial.dto.event.OrderCreatedEvent;
import edgareldy.springkafkatutorial.messaging.producer.OrderEventProducer;
import edgareldy.springkafkatutorial.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to {@code order-events} and decrements the corresponding
 * product's stock through {@link StockService}, never touching
 * {@code ProductRepository} directly so that logic stays centralized in
 * one place. Delivery is at-least-once (this project's chosen semantics,
 * see the README's Kafka fundamentals section): a crash between processing
 * and offset commit redelivers the same event, so {@code decrementStock}
 * can run more than once for one order. A record that keeps throwing is
 * retried by the {@code DefaultErrorHandler} configured in
 * {@code KafkaConsumerConfig}, then routed to the dead-letter topic rather
 * than blocking this partition forever.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final StockService stockService;

    @KafkaListener(topics = OrderEventProducer.ORDER_EVENTS_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderCreatedEvent event) {
        log.info("Consumed OrderCreatedEvent for order {}: decrementing stock of product {} by {}",
                event.orderId(), event.productId(), event.quantity());
        stockService.decrementStock(event.productId(), event.quantity());
    }
}
