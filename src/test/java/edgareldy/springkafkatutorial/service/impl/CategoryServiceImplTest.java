package edgareldy.springkafkatutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springkafkatutorial.dto.category.CategoryRequest;
import edgareldy.springkafkatutorial.dto.category.CategoryResponse;
import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.entity.Category;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.mapper.CategoryMapper;
import edgareldy.springkafkatutorial.repository.CategoryRepository;
import edgareldy.springkafkatutorial.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for {@link CategoryServiceImpl}, with {@link CategoryRepository},
 * {@link ProductRepository}, and {@link CategoryMapper} mocked.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).categoryName("Electronics").build();
        categoryResponse = new CategoryResponse(1L, "Electronics");
    }

    @Test
    void findAllReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category), pageable, 1));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        PageResponse<CategoryResponse> result = categoryService.findAll(pageable);

        assertThat(result.content()).containsExactly(categoryResponse);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void findByIdReturnsResponseWhenFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        assertThat(categoryService.findById(1L)).isEqualTo(categoryResponse);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSavesAndReturnsResponse() {
        CategoryRequest request = new CategoryRequest("Electronics");
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        assertThat(categoryService.create(request)).isEqualTo(categoryResponse);
    }

    @Test
    void updateAppliesRequestAndReturnsResponse() {
        CategoryRequest request = new CategoryRequest("Home Appliances");
        CategoryResponse updatedResponse = new CategoryResponse(1L, "Home Appliances");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(updatedResponse);

        assertThat(categoryService.update(1L, request)).isEqualTo(updatedResponse);

        verify(categoryMapper).updateEntityFromRequest(request, category);
    }

    @Test
    void updateThrowsWhenMissing() {
        CategoryRequest request = new CategoryRequest("Home Appliances");
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteRemovesCategoryWhenEmpty() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteThrowsBusinessRuleExceptionWhenCategoryHasProducts() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteThrowsResourceNotFoundExceptionWhenMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
