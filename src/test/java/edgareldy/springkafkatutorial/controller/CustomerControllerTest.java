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
import edgareldy.springkafkatutorial.dto.customer.CustomerRequest;
import edgareldy.springkafkatutorial.dto.customer.CustomerResponse;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.service.CustomerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc integration tests for {@link CustomerController}, with
 * {@link CustomerService} mocked.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private static CustomerRequest validRequest() {
        return new CustomerRequest("Ada", "Lovelace", "+1 202-555-0100", "ada@example.com", "1 Analytical Engine Way");
    }

    private static CustomerResponse savedResponse() {
        return new CustomerResponse(
                1L, "Ada", "Lovelace", "+1 202-555-0100", "ada@example.com", "1 Analytical Engine Way");
    }

    @Test
    void findAllWithoutSearchPassesNullThrough() throws Exception {
        PageResponse<CustomerResponse> page = new PageResponse<>(List.of(savedResponse()), 0, 20, 1, 1);
        when(customerService.findAll(isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].lastName").value("Lovelace"));
    }

    @Test
    void findAllWithSearchForwardsTerm() throws Exception {
        PageResponse<CustomerResponse> page = new PageResponse<>(List.of(savedResponse()), 0, 20, 1, 1);
        when(customerService.findAll(eq("lovelace"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers").param("search", "lovelace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Ada"));
    }

    @Test
    void findByIdReturns404WhenMissing() throws Exception {
        when(customerService.findById(99L)).thenThrow(new ResourceNotFoundException("Customer not found with id 99"));

        mockMvc.perform(get("/api/v1/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createReturns201WhenValid() throws Exception {
        when(customerService.create(any())).thenReturn(savedResponse());

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("ada@example.com"));
    }

    @Test
    void createReturns400WhenEmailInvalid() throws Exception {
        CustomerRequest invalid = new CustomerRequest(
                "Ada", "Lovelace", "+1 202-555-0100", "not-an-email", "1 Analytical Engine Way");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createReturns422WhenEmailAlreadyUsed() throws Exception {
        when(customerService.create(any()))
                .thenThrow(new BusinessRuleException("Email ada@example.com is already in use"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void updateReturns200WhenValid() throws Exception {
        when(customerService.update(eq(1L), any())).thenReturn(savedResponse());

        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("ada@example.com"));
    }

    @Test
    void updateReturns404WhenMissing() throws Exception {
        when(customerService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Customer not found with id 99"));

        mockMvc.perform(put("/api/v1/customers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns200WhenSuccessful() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Customer not found with id 99"))
                .when(customerService).delete(99L);

        mockMvc.perform(delete("/api/v1/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
