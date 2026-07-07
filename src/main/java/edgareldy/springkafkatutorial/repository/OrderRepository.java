package edgareldy.springkafkatutorial.repository;

import edgareldy.springkafkatutorial.entity.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for {@link Order}.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Overrides the base paginated find to eagerly fetch each order's
     * customer and product, avoiding two N+1 selects per row when the
     * controller maps a page of orders to {@code OrderResponse}.
     */
    @EntityGraph(attributePaths = {"customer", "product"})
    @Override
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "product"})
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "product"})
    Page<Order> findByProductId(Long productId, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.product WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}
