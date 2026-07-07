package edgareldy.springkafkatutorial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edgareldy.springkafkatutorial.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * {@code @DataJpaTest} for {@link CategoryRepository}, backed by a real
 * PostgreSQL instance via Testcontainers rather than an in-memory database,
 * so the schema and constraints exercised match dev/prod exactly.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RepositoryTestcontainersConfiguration.class)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void savesAndFindsCategoryById() {
        Category saved = categoryRepository.save(Category.builder().categoryName("Books").build());

        assertThat(categoryRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Category::getCategoryName)
                .isEqualTo("Books");
    }

    @Test
    void findAllReturnsSavedCategories() {
        categoryRepository.save(Category.builder().categoryName("Toys").build());
        categoryRepository.save(Category.builder().categoryName("Garden").build());

        assertThat(categoryRepository.findAll()).hasSize(2);
    }
}
