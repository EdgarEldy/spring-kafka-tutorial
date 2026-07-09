package edgareldy.springkafkatutorial.service.impl;

import edgareldy.springkafkatutorial.entity.Product;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.repository.ProductRepository;
import edgareldy.springkafkatutorial.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link StockService} implementation backed by
 * {@link ProductRepository}. This project uses at-least-once Kafka
 * delivery, so {@link #decrementStock(Long, int)} can be invoked more than
 * once for the same order if the consumer crashes after processing but
 * before committing its offset: this method does not itself deduplicate,
 * it simply re-applies the decrement, which can double count stock on a
 * redelivery. True exactly-once processing would need an idempotency key
 * (e.g. a processed-order-ids table) that this tutorial deliberately does
 * not implement, see the README's Kafka fundamentals section.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void decrementStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));

        int newStockQuantity = product.getStockQuantity() - quantity;
        if (newStockQuantity < 0) {
            throw new BusinessRuleException(
                    "Product with id " + productId + " does not have enough stock to decrement by " + quantity);
        }

        product.setStockQuantity(newStockQuantity);
        productRepository.save(product);
    }
}
