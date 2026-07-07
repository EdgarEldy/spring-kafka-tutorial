package edgareldy.springkafkatutorial.service;

import edgareldy.springkafkatutorial.dto.category.CategoryRequest;
import edgareldy.springkafkatutorial.dto.category.CategoryResponse;
import edgareldy.springkafkatutorial.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Contract for {@link edgareldy.springkafkatutorial.entity.Category} business
 * operations. Controllers and tests depend on this interface, never on its
 * implementation directly.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public interface CategoryService {

    PageResponse<CategoryResponse> findAll(Pageable pageable);

    CategoryResponse findById(Long id);

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
