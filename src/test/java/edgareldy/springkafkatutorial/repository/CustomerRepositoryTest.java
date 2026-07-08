package edgareldy.springkafkatutorial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edgareldy.springkafkatutorial.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

/**
 * {@code @DataJpaTest} for {@link CustomerRepository}, backed by a real
 * PostgreSQL instance via Testcontainers rather than an in-memory database,
 * so the unique email constraint and the search query are exercised against
 * the same engine as dev/prod.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RepositoryTestcontainersConfiguration.class)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.save(Customer.builder()
                .firstName("Ada").lastName("Lovelace").telephone("+1 202-555-0100")
                .email("ada@example.com").address("1 Analytical Engine Way").build());
        customerRepository.save(Customer.builder()
                .firstName("Grace").lastName("Hopper").telephone("+1 202-555-0101")
                .email("grace@example.com").address("2 Analytical Engine Way").build());
    }

    @Test
    void findByEmailReturnsMatchingCustomer() {
        assertThat(customerRepository.findByEmail("ada@example.com"))
                .isPresent()
                .get()
                .extracting(Customer::getLastName)
                .isEqualTo("Lovelace");
    }

    @Test
    void existsByEmailIgnoreCaseReflectsCurrentData() {
        assertThat(customerRepository.existsByEmailIgnoreCase("ada@example.com")).isTrue();
        assertThat(customerRepository.existsByEmailIgnoreCase("unknown@example.com")).isFalse();
    }

    @Test
    void existsByEmailIgnoreCaseMatchesRegardlessOfCase() {
        assertThat(customerRepository.existsByEmailIgnoreCase("ADA@EXAMPLE.COM")).isTrue();
    }

    @Test
    void searchMatchesCaseInsensitiveFullName() {
        var page = customerRepository.search("ada lovelace", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Customer::getEmail).containsExactly("ada@example.com");
    }
}
