package edgareldy.springkafkatutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springkafkatutorial.entity.Category;
import edgareldy.springkafkatutorial.entity.Product;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.repository.ProductRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link StockServiceImpl}, with {@link ProductRepository}
 * mocked.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(1L).categoryName("Electronics").build();
        product = Product.builder().id(1L).category(category).productName("Keyboard")
                .unitPrice(79.99f).stockQuantity(10).build();
    }

    @Test
    void decrementStockReducesStockQuantityWhenSufficient() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        stockService.decrementStock(1L, 4);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getStockQuantity()).isEqualTo(6);
    }

    @Test
    void decrementStockAllowsReachingExactlyZero() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        stockService.decrementStock(1L, 10);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getStockQuantity()).isZero();
    }

    @Test
    void decrementStockThrowsWhenQuantityExceedsStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> stockService.decrementStock(1L, 11))
                .isInstanceOf(BusinessRuleException.class);

        verify(productRepository, never()).save(product);
    }

    @Test
    void decrementStockThrowsWhenProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.decrementStock(99L, 1))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
