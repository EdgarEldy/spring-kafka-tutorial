package edgareldy.springkafkatutorial.service.impl;

import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.customer.CustomerRequest;
import edgareldy.springkafkatutorial.dto.customer.CustomerResponse;
import edgareldy.springkafkatutorial.entity.Customer;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.mapper.CustomerMapper;
import edgareldy.springkafkatutorial.repository.CustomerRepository;
import edgareldy.springkafkatutorial.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Default {@link CustomerService} implementation backed by
 * {@link CustomerRepository}.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public PageResponse<CustomerResponse> findAll(String search, Pageable pageable) {
        Page<Customer> page = StringUtils.hasText(search)
                ? customerRepository.search(search, pageable)
                : customerRepository.findAll(pageable);
        return PageResponse.from(page.map(customerMapper::toResponse));
    }

    @Override
    public CustomerResponse findById(Long id) {
        return customerMapper.toResponse(getCustomerOrThrow(id));
    }

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Email " + request.email() + " is already in use");
        }
        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getCustomerOrThrow(id);
        if (!customer.getEmail().equalsIgnoreCase(request.email())
                && customerRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Email " + request.email() + " is already in use");
        }
        customerMapper.updateEntityFromRequest(request, customer);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = getCustomerOrThrow(id);
        customerRepository.delete(customer);
    }

    private Customer getCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }
}
