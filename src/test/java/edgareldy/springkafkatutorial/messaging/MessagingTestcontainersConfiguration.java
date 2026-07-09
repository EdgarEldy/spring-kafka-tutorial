package edgareldy.springkafkatutorial.messaging;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Provides Testcontainers Postgres and Kafka instances for
 * {@code @SpringBootTest} classes in this package, mirroring the top-level
 * {@code TestcontainersConfiguration}. Kept local to {@code messaging/}
 * rather than reusing that package-private class from a different package.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@TestConfiguration(proxyBeanMethods = false)
class MessagingTestcontainersConfiguration {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"));
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));
    }
}
