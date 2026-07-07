package edgareldy.springkafkatutorial.repository;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Provides a Testcontainers Postgres instance for {@code @DataJpaTest}
 * classes in this package, mirroring the top-level
 * {@code TestcontainersConfiguration} used by full {@code @SpringBootTest}
 * classes. Kept local to {@code repository/} rather than reusing that
 * package-private class from a different package.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@TestConfiguration(proxyBeanMethods = false)
class RepositoryTestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));
    }
}
