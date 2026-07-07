package edgareldy.springkafkatutorial.service;

import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.customer.CustomerRequest;
import edgareldy.springkafkatutorial.dto.customer.CustomerResponse;
import org.springframework.data.domain.Pageable;

/**
 * Contract for {@link edgareldy.springkafkatutorial.entity.Customer} business
 * operations. Controllers and tests depend on this interface, never on its
 * implementation directly.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public interface CustomerService {

    PageResponse<CustomerResponse> findAll(String search, Pageable pageable);

    CustomerResponse findById(Long id);

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);
}
