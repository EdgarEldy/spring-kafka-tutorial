package edgareldy.springkafkatutorial.repository;

import edgareldy.springkafkatutorial.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for {@link Product}.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Overrides the base paginated find to eagerly fetch each product's
     * category, avoiding one N+1 select per row when the controller maps a
     * page of products to {@code ProductResponse}.
     */
    @EntityGraph(attributePaths = "category")
    @Override
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    boolean existsByCategoryId(Long categoryId);
}
