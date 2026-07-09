package edgareldy.springkafkatutorial.dto.event;

/**
 * Payload published to the {@code order-events} Kafka topic when an order
 * is created. Deliberately independent of the {@code Order} JPA entity:
 * only the fields the consumer actually needs to decrement stock, never
 * the entity itself or its lazy associations.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
public record OrderCreatedEvent(
        Long orderId,
        Long productId,
        int quantity
) {
}
