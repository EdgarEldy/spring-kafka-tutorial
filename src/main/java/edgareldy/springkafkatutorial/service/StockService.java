package edgareldy.springkafkatutorial.service;

/**
 * Contract for stock adjustments on a {@link edgareldy.springkafkatutorial.entity.Product}.
 * The only caller today is {@code OrderEventConsumer}, reacting to a
 * consumed {@code OrderCreatedEvent}, but the operation is exposed as its
 * own service rather than folded into {@code ProductService} so the stock
 * decrement stays centralized in one place, never duplicated by anything
 * that also happens to touch a product.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
public interface StockService {

    void decrementStock(Long productId, int quantity);
}
