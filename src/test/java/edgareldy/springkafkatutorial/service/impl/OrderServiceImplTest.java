package edgareldy.springkafkatutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.event.OrderCreatedEvent;
import edgareldy.springkafkatutorial.dto.order.OrderRequest;
import edgareldy.springkafkatutorial.dto.order.OrderResponse;
import edgareldy.springkafkatutorial.entity.Category;
import edgareldy.springkafkatutorial.entity.Customer;
import edgareldy.springkafkatutorial.entity.Order;
import edgareldy.springkafkatutorial.entity.Product;
import edgareldy.springkafkatutorial.exception.BusinessRuleException;
import edgareldy.springkafkatutorial.exception.ResourceNotFoundException;
import edgareldy.springkafkatutorial.mapper.OrderMapper;
import edgareldy.springkafkatutorial.messaging.producer.OrderEventProducer;
import edgareldy.springkafkatutorial.repository.CustomerRepository;
import edgareldy.springkafkatutorial.repository.OrderRepository;
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
 * Unit tests for {@link OrderServiceImpl}, with {@link OrderRepository},
 * {@link CustomerRepository}, {@link ProductRepository},
 * {@link OrderMapper}, and {@link OrderEventProducer} mocked.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customer;
    private Product product;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(1L).categoryName("Electronics").build();
        customer = Customer.builder().id(1L).firstName("Ada").lastName("Lovelace")
                .telephone("+1 202-555-0100").email("ada@example.com").address("1 Analytical Engine Way").build();
        product = Product.builder().id(1L).category(category).productName("Keyboard")
                .unitPrice(79.99f).stockQuantity(50).build();
        order = Order.builder().id(1L).customer(customer).product(product).quantity(2).total(159.98).build();
        orderResponse = new OrderResponse(1L, 1L, "Ada", "Lovelace", 1L, "Keyboard", 2, 159.98);
    }

    @Test
    void findAllWithoutFilterUsesPlainFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(order), pageable, 1));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        PageResponse<OrderResponse> result = orderService.findAll(null, null, pageable);

        assertThat(result.content()).containsExactly(orderResponse);
    }

    @Test
    void findAllWithCustomerIdFiltersByCustomer() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(new PageImpl<>(List.of(order), pageable, 1));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        PageResponse<OrderResponse> result = orderService.findAll(1L, null, pageable);

        assertThat(result.content()).containsExactly(orderResponse);
        verify(orderRepository, never()).findAll(pageable);
        verify(orderRepository, never()).findByProductId(any(), any());
    }

    @Test
    void findAllWithProductIdFiltersByProduct() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findByProductId(1L, pageable)).thenReturn(new PageImpl<>(List.of(order), pageable, 1));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        PageResponse<OrderResponse> result = orderService.findAll(null, 1L, pageable);

        assertThat(result.content()).containsExactly(orderResponse);
        verify(orderRepository, never()).findAll(pageable);
        verify(orderRepository, never()).findByCustomerId(any(), any());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(orderRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createComputesTotalAndSavesWhenStockSufficient() {
        OrderRequest request = new OrderRequest(1L, 1L, 2);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        assertThat(orderService.create(request)).isEqualTo(orderResponse);

        assertThat(order.getTotal()).isEqualTo(79.99f * 2);
    }

    @Test
    void createPublishesOrderCreatedEventAfterSaving() {
        OrderRequest request = new OrderRequest(1L, 1L, 2);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        orderService.create(request);

        verify(orderEventProducer).publish(new OrderCreatedEvent(order.getId(), product.getId(), 2));
    }

    @Test
    void createThrowsWhenCustomerMissing() {
        OrderRequest request = new OrderRequest(99L, 1L, 2);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).save(any());
        verify(orderEventProducer, never()).publish(any());
    }

    @Test
    void createThrowsWhenProductMissing() {
        OrderRequest request = new OrderRequest(1L, 99L, 2);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).save(any());
        verify(orderEventProducer, never()).publish(any());
    }

    @Test
    void createThrowsWhenStockInsufficient() {
        OrderRequest request = new OrderRequest(1L, 1L, 100);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(orderRepository, never()).save(any());
        verify(orderEventProducer, never()).publish(any());
    }

    @Test
    void updateRecomputesTotalWhenStockSufficient() {
        OrderRequest request = new OrderRequest(1L, 1L, 3);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        assertThat(orderService.update(1L, request)).isEqualTo(orderResponse);

        assertThat(order.getTotal()).isEqualTo(79.99f * 3);
        verify(orderMapper).updateEntityFromRequest(request, order);
        verify(orderEventProducer, never()).publish(any());
    }

    @Test
    void updateThrowsWhenOrderMissing() {
        OrderRequest request = new OrderRequest(1L, 1L, 2);
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateThrowsWhenStockInsufficient() {
        OrderRequest request = new OrderRequest(1L, 1L, 100);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.update(1L, request))
                .isInstanceOf(BusinessRuleException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteRemovesOrderWhenExists() {
        when(orderRepository.existsById(1L)).thenReturn(true);

        orderService.delete(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).deleteById(any());
    }
}
