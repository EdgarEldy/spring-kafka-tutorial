package edgareldy.springkafkatutorial;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Smoke test verifying that the Spring application context loads with the Testcontainers
 * backed PostgreSQL and Kafka dependencies.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-kafka-tutorial
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SpringKafkaTutorialApplicationTests {

    @Test
    void contextLoads() {
    }

}
