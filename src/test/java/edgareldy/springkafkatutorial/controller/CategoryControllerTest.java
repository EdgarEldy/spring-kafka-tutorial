package edgareldy.springkafkatutorial.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edgareldy.springkafkatutorial.dto.category.CategoryRequest;
import edgareldy.springkafkatutorial.dto.category.CategoryResponse;
import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc integration tests for {@link CategoryController}, with
 * {@link CategoryService} mocked.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void findAllReturnsPagedCategories() throws Exception {
        CategoryResponse category = new CategoryResponse(1L, "Electronics");
        PageResponse<CategoryResponse> page = new PageResponse<>(List.of(category), 0, 20, 1, 1);
        when(categoryService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].categoryName").value("Electronics"));
    }

    @Test
    void findByIdReturnsCategoryWhenFound() throws Exception {
        when(categoryService.findById(1L)).thenReturn(new CategoryResponse(1L, "Electronics"));

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").value("Electronics"));
    }

    @Test
    void findByIdReturns404WhenMissing() throws Exception {
        when(categoryService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Category not found with id 99"));

        mockMvc.perform(get("/api/v1/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createReturns201WhenValid() throws Exception {
        when(categoryService.create(any())).thenReturn(new CategoryResponse(2L, "Books"));

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Books"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.categoryName").value("Books"));
    }

    @Test
    void createReturns400WhenNameBlank() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateReturns200WhenValid() throws Exception {
        when(categoryService.update(eq(1L), any())).thenReturn(new CategoryResponse(1L, "Home Appliances"));

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Home Appliances"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").value("Home Appliances"));
    }

    @Test
    void updateReturns404WhenMissing() throws Exception {
        when(categoryService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Category not found with id 99"));

        mockMvc.perform(put("/api/v1/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Home Appliances"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteReturns200WhenSuccessful() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found with id 99"))
                .when(categoryService).delete(99L);

        mockMvc.perform(delete("/api/v1/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteReturns422WhenCategoryHasProducts() throws Exception {
        doThrow(new BusinessRuleException("Category with id 1 still has products and cannot be deleted"))
                .when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }
}
