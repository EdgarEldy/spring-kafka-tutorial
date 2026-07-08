package edgareldy.springkafkatutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.customer.CustomerRequest;
import edgareldy.springkafkatutorial.dto.customer.CustomerResponse;
import edgareldy.springkafkatutorial.entity.Customer;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.mapper.CustomerMapper;
import edgareldy.springkafkatutorial.repository.CustomerRepository;
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
 * Unit tests for {@link CustomerServiceImpl}, with {@link CustomerRepository}
 * and {@link CustomerMapper} mocked.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L).firstName("Ada").lastName("Lovelace").telephone("+1 202-555-0100")
                .email("ada@example.com").address("1 Analytical Engine Way").build();
        customerResponse = new CustomerResponse(
                1L, "Ada", "Lovelace", "+1 202-555-0100", "ada@example.com", "1 Analytical Engine Way");
    }

    @Test
    void findAllWithoutSearchUsesPlainFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(customerRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(customer), pageable, 1));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        PageResponse<CustomerResponse> result = customerService.findAll(null, pageable);

        assertThat(result.content()).containsExactly(customerResponse);
        verify(customerRepository, never()).search(any(), any());
    }

    @Test
    void findAllWithSearchUsesSearchQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        when(customerRepository.search("lovelace", pageable))
                .thenReturn(new PageImpl<>(List.of(customer), pageable, 1));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        PageResponse<CustomerResponse> result = customerService.findAll("lovelace", pageable);

        assertThat(result.content()).containsExactly(customerResponse);
        verify(customerRepository, never()).findAll(pageable);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSavesWhenEmailUnused() {
        CustomerRequest request = new CustomerRequest(
                "Ada", "Lovelace", "+1 202-555-0100", "ada@example.com", "1 Analytical Engine Way");
        when(customerRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(false);
        when(customerMapper.toEntity(request)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        assertThat(customerService.create(request)).isEqualTo(customerResponse);
    }

    @Test
    void createThrowsWhenEmailAlreadyUsed() {
        CustomerRequest request = new CustomerRequest(
                "Ada", "Lovelace", "+1 202-555-0100", "ada@example.com", "1 Analytical Engine Way");
        when(customerRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenEmailAlreadyUsedWithDifferentCase() {
        CustomerRequest request = new CustomerRequest(
                "Ada", "Lovelace", "+1 202-555-0100", "ADA@EXAMPLE.COM", "1 Analytical Engine Way");
        when(customerRepository.existsByEmailIgnoreCase("ADA@EXAMPLE.COM")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateAppliesRequestWhenEmailUnchanged() {
        CustomerRequest request = new CustomerRequest(
                "Ada", "Byron", "+1 202-555-0100", "ada@example.com", "2 Analytical Engine Way");
        CustomerResponse updatedResponse = new CustomerResponse(
                1L, "Ada", "Byron", "+1 202-555-0100", "ada@example.com", "2 Analytical Engine Way");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(updatedResponse);

        assertThat(customerService.update(1L, request)).isEqualTo(updatedResponse);

        verify(customerRepository, never()).existsByEmailIgnoreCase(any());
        verify(customerMapper).updateEntityFromRequest(request, customer);
    }

    @Test
    void updateAppliesRequestWhenEmailUnchangedOnlyByCase() {
        CustomerRequest request = new CustomerRequest(
                "Ada", "Byron", "+1 202-555-0100", "ADA@EXAMPLE.COM", "2 Analytical Engine Way");
        CustomerResponse updatedResponse = new CustomerResponse(
                1L, "Ada", "Byron", "+1 202-555-0100", "ADA@EXAMPLE.COM", "2 Analytical Engine Way");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(updatedResponse);

        assertThat(customerService.update(1L, request)).isEqualTo(updatedResponse);

        verify(customerRepository, never()).existsByEmailIgnoreCase(any());
    }

    @Test
    void updateThrowsWhenNewEmailAlreadyUsedByAnotherCustomer() {
        CustomerRequest request = new CustomerRequest(
                "Ada", "Lovelace", "+1 202-555-0100", "ada.lovelace@example.com", "1 Analytical Engine Way");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmailIgnoreCase("ada.lovelace@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.update(1L, request))
                .isInstanceOf(BusinessRuleException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateThrowsWhenMissing() {
        CustomerRequest request = new CustomerRequest(
                "Ada", "Lovelace", "+1 202-555-0100", "ada@example.com", "1 Analytical Engine Way");
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesCustomerWhenExists() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.delete(1L);

        verify(customerRepository).delete(customer);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
