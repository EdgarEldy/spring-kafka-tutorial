package edgareldy.springkafkatutorial.repository;

import edgareldy.springkafkatutorial.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Category}.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
