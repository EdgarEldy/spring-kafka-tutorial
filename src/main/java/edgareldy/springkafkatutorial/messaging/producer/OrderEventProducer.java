package edgareldy.springkafkatutorial.messaging.producer;

import edgareldy.springkafkatutorial.dto.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publishes {@link OrderCreatedEvent} to the {@code order-events} topic,
 * keyed by {@code productId} so Kafka always routes events for the same
 * product to the same partition, guaranteeing per-product ordering. If a
 * transaction is active when {@link #publish(OrderCreatedEvent)} is called,
 * the actual send is deferred until that transaction commits, via
 * {@link TransactionSynchronizationManager}: publishing before commit risks
 * announcing an order that a later rollback makes disappear.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String ORDER_EVENTS_DLT_TOPIC = "order-events-dlt";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void publish(OrderCreatedEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    private void send(OrderCreatedEvent event) {
        String partitionKey = String.valueOf(event.productId());
        log.info("Publishing OrderCreatedEvent for order {} to topic {} with key {}",
                event.orderId(), ORDER_EVENTS_TOPIC, partitionKey);
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, partitionKey, event);
    }
}
