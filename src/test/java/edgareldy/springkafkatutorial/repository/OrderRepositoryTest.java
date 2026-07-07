package edgareldy.springkafkatutorial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edgareldy.springkafkatutorial.entity.Category;
import edgareldy.springkafkatutorial.entity.Customer;
import edgareldy.springkafkatutorial.entity.Order;
import edgareldy.springkafkatutorial.entity.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

/**
 * {@code @DataJpaTest} for {@link OrderRepository}, backed by a real
 * PostgreSQL instance via Testcontainers.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RepositoryTestcontainersConfiguration.class)
class OrderRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer ada;
    private Customer grace;
    private Product keyboard;
    private Product desk;

    @BeforeEach
    void setUp() {
        Category electronics = categoryRepository.save(Category.builder().categoryName("Electronics").build());
        keyboard = productRepository.save(Product.builder()
                .category(electronics).productName("Keyboard").unitPrice(79.99f).stockQuantity(50).build());
        desk = productRepository.save(Product.builder()
                .category(electronics).productName("Desk").unitPrice(199.99f).stockQuantity(10).build());

        ada = customerRepository.save(Customer.builder()
                .firstName("Ada").lastName("Lovelace").telephone("+1 202-555-0100")
                .email("ada@example.com").address("1 Analytical Engine Way").build());
        grace = customerRepository.save(Customer.builder()
                .firstName("Grace").lastName("Hopper").telephone("+1 202-555-0101")
                .email("grace@example.com").address("2 Analytical Engine Way").build());

        orderRepository.save(Order.builder().customer(ada).product(keyboard).quantity(2).total(159.98).build());
        orderRepository.save(Order.builder().customer(grace).product(desk).quantity(1).total(199.99).build());
    }

    @Test
    void findAllEagerlyLoadsCustomerAndProductForEveryOrder() {
        var page = orderRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        for (Order order : page.getContent()) {
            boolean customerLoaded = entityManager.getEntityManagerFactory()
                    .getPersistenceUnitUtil().isLoaded(order, "customer");
            boolean productLoaded = entityManager.getEntityManagerFactory()
                    .getPersistenceUnitUtil().isLoaded(order, "product");
            assertThat(customerLoaded).as("customer should be eagerly fetched by @EntityGraph").isTrue();
            assertThat(productLoaded).as("product should be eagerly fetched by @EntityGraph").isTrue();
        }
    }

    @Test
    void findByCustomerIdReturnsOnlyMatchingOrders() {
        var page = orderRepository.findByCustomerId(ada.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(o -> o.getProduct().getProductName()).containsExactly("Keyboard");
    }

    @Test
    void findByProductIdReturnsOnlyMatchingOrders() {
        var page = orderRepository.findByProductId(desk.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(o -> o.getCustomer().getLastName()).containsExactly("Hopper");
    }

    @Test
    void findByIdWithDetailsEagerlyLoadsCustomerAndProduct() {
        Long orderId = orderRepository.findByCustomerId(ada.getId(), PageRequest.of(0, 10))
                .getContent().get(0).getId();

        var found = orderRepository.findByIdWithDetails(orderId);

        assertThat(found).isPresent();
        assertThat(found.get().getCustomer().getFirstName()).isEqualTo("Ada");
        assertThat(found.get().getProduct().getProductName()).isEqualTo("Keyboard");
    }
}
