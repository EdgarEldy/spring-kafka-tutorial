package edgareldy.springkafkatutorial.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the Swagger UI metadata (title, description, version) shown at
 * the top of the generated OpenAPI documentation.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-kafka-tutorial
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springKafkaTutorialOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Kafka Tutorial API")
                        .description("CRUD REST API demonstrating Kafka integration with Spring Boot: "
                                + "categories, products, customers, orders and asynchronous stock "
                                + "decrement through an OrderCreatedEvent.")
                        .version("1.0.0"));
    }
}
