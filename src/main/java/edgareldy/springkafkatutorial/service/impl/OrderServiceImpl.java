package edgareldy.springkafkatutorial.service.impl;

import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.order.OrderRequest;
import edgareldy.springkafkatutorial.dto.order.OrderResponse;
import edgareldy.springkafkatutorial.entity.Customer;
import edgareldy.springkafkatutorial.entity.Order;
import edgareldy.springkafkatutorial.entity.Product;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.mapper.OrderMapper;
import edgareldy.springkafkatutorial.repository.CustomerRepository;
import edgareldy.springkafkatutorial.repository.OrderRepository;
import edgareldy.springkafkatutorial.repository.ProductRepository;
import edgareldy.springkafkatutorial.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link OrderService} implementation backed by
 * {@link OrderRepository}. Checks that the product has enough stock before
 * accepting an order, but never decrements {@code stockQuantity} itself:
 * that happens asynchronously, once an {@code OrderCreatedEvent} is
 * consumed from Kafka in feature/messaging. This is the classic use case
 * for going asynchronous documented in the README: order creation responds
 * to the caller immediately, without waiting for the stock update.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Override
    public PageResponse<OrderResponse> findAll(Long customerId, Long productId, Pageable pageable) {
        Page<Order> page;
        if (customerId != null) {
            page = orderRepository.findByCustomerId(customerId, pageable);
        } else if (productId != null) {
            page = orderRepository.findByProductId(productId, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(orderMapper::toResponse));
    }

    @Override
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        Customer customer = getCustomerOrThrow(request.customerId());
        Product product = getProductOrThrow(request.productId());
        checkSufficientStock(product, request.quantity());

        Order order = orderMapper.toEntity(request);
        order.setCustomer(customer);
        order.setProduct(product);
        order.setTotal(computeTotal(product, request.quantity()));
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
        Customer customer = getCustomerOrThrow(request.customerId());
        Product product = getProductOrThrow(request.productId());
        checkSufficientStock(product, request.quantity());

        orderMapper.updateEntityFromRequest(request, order);
        order.setCustomer(customer);
        order.setProduct(product);
        order.setTotal(computeTotal(product, request.quantity()));
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id " + id);
        }
        orderRepository.deleteById(id);
    }

    private Customer getCustomerOrThrow(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));
    }

    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));
    }

    private void checkSufficientStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new BusinessRuleException("Product with id " + product.getId()
                    + " does not have enough stock for quantity " + quantity);
        }
    }

    private double computeTotal(Product product, int quantity) {
        return product.getUnitPrice() * quantity;
    }
}
