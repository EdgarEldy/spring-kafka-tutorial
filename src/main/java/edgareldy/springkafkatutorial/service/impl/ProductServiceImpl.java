package edgareldy.springkafkatutorial.service.impl;

import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.product.ProductRequest;
import edgareldy.springkafkatutorial.dto.product.ProductResponse;
import edgareldy.springkafkatutorial.entity.Category;
import edgareldy.springkafkatutorial.entity.Product;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.mapper.ProductMapper;
import edgareldy.springkafkatutorial.repository.CategoryRepository;
import edgareldy.springkafkatutorial.repository.OrderRepository;
import edgareldy.springkafkatutorial.repository.ProductRepository;
import edgareldy.springkafkatutorial.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link ProductService} implementation backed by
 * {@link ProductRepository}. {@code stockQuantity} is written here exactly
 * like any other request field (initial value on create, replacement value
 * on update); the decrement triggered by Kafka consumption lives in
 * {@code StockService} (feature/messaging), never duplicated here.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final ProductMapper productMapper;

    @Override
    public PageResponse<ProductResponse> findAll(Long categoryId, Pageable pageable) {
        Page<Product> page = categoryId != null
                ? productRepository.findByCategoryId(categoryId, pageable)
                : productRepository.findAll(pageable);
        return PageResponse.from(page.map(productMapper::toResponse));
    }

    @Override
    public ProductResponse findById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = getCategoryOrThrow(request.categoryId());
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        Category category = getCategoryOrThrow(request.categoryId());
        productMapper.updateEntityFromRequest(request, product);
        product.setCategory(category);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }
        if (orderRepository.existsByProductId(id)) {
            throw new BusinessRuleException(
                    "Product with id " + id + " still has orders and cannot be deleted");
        }
        productRepository.deleteById(id);
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
    }
}
