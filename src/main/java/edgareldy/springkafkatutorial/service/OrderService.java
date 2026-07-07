package edgareldy.springkafkatutorial.service;

import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.order.OrderRequest;
import edgareldy.springkafkatutorial.dto.order.OrderResponse;
import org.springframework.data.domain.Pageable;

/**
 * Contract for {@link edgareldy.springkafkatutorial.entity.Order} business
 * operations. Controllers and tests depend on this interface, never on its
 * implementation directly. No Kafka publication happens through this
 * contract: that wiring is added on top of {@code create} only in
 * feature/messaging.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public interface OrderService {

    PageResponse<OrderResponse> findAll(Long customerId, Long productId, Pageable pageable);

    OrderResponse findById(Long id);

    OrderResponse create(OrderRequest request);

    OrderResponse update(Long id, OrderRequest request);

    void delete(Long id);
}
