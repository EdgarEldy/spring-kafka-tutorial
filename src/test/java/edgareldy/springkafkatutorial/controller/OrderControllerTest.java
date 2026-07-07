package edgareldy.springkafkatutorial.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.order.OrderRequest;
import edgareldy.springkafkatutorial.dto.order.OrderResponse;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.service.OrderService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc integration tests for {@link OrderController}, with
 * {@link OrderService} mocked.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private static OrderResponse savedResponse() {
        return new OrderResponse(1L, 1L, "Ada", "Lovelace", 1L, "Keyboard", 2, 159.98);
    }

    @Test
    void findAllWithoutFilterPassesNullsThrough() throws Exception {
        PageResponse<OrderResponse> page = new PageResponse<>(List.of(savedResponse()), 0, 20, 1, 1);
        when(orderService.findAll(isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].productName").value("Keyboard"));
    }

    @Test
    void findAllWithCustomerIdForwardsFilter() throws Exception {
        PageResponse<OrderResponse> page = new PageResponse<>(List.of(savedResponse()), 0, 20, 1, 1);
        when(orderService.findAll(eq(1L), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders").param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].customerId").value(1));
    }

    @Test
    void findByIdReturns404WhenMissing() throws Exception {
        when(orderService.findById(99L)).thenThrow(new ResourceNotFoundException("Order not found with id 99"));

        mockMvc.perform(get("/api/v1/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createReturns201WhenValid() throws Exception {
        OrderRequest request = new OrderRequest(1L, 1L, 2);
        when(orderService.create(any())).thenReturn(savedResponse());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.total").value(159.98));
    }

    @Test
    void createReturns400WhenQuantityNotPositive() throws Exception {
        OrderRequest invalid = new OrderRequest(1L, 1L, 0);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createReturns404WhenCustomerOrProductMissing() throws Exception {
        OrderRequest request = new OrderRequest(99L, 1L, 2);
        when(orderService.create(any())).thenThrow(new ResourceNotFoundException("Customer not found with id 99"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReturns422WhenStockInsufficient() throws Exception {
        OrderRequest request = new OrderRequest(1L, 1L, 1000);
        when(orderService.create(any()))
                .thenThrow(new BusinessRuleException("Product with id 1 does not have enough stock for quantity 1000"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void updateReturns200WhenValid() throws Exception {
        OrderRequest request = new OrderRequest(1L, 1L, 3);
        when(orderService.update(eq(1L), any())).thenReturn(savedResponse());

        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productName").value("Keyboard"));
    }

    @Test
    void updateReturns404WhenOrderMissing() throws Exception {
        OrderRequest request = new OrderRequest(1L, 1L, 3);
        when(orderService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Order not found with id 99"));

        mockMvc.perform(put("/api/v1/orders/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns200WhenSuccessful() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Order not found with id 99"))
                .when(orderService).delete(99L);

        mockMvc.perform(delete("/api/v1/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
