package edgareldy.springkafkatutorial.service;

import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.product.ProductRequest;
import edgareldy.springkafkatutorial.dto.product.ProductResponse;
import org.springframework.data.domain.Pageable;

/**
 * Contract for {@link edgareldy.springkafkatutorial.entity.Product} business
 * operations. Controllers and tests depend on this interface, never on its
 * implementation directly. Stock decrement triggered by Kafka consumption
 * is not part of this contract, see {@code StockService} (feature/messaging).
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public interface ProductService {

    PageResponse<ProductResponse> findAll(Long categoryId, Pageable pageable);

    ProductResponse findById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
