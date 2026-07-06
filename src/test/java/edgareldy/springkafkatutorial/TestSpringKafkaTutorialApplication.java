package edgareldy.springkafkatutorial;

import org.springframework.boot.SpringApplication;

/**
 * Local runner that boots the application with the Testcontainers configuration attached,
 * useful to start PostgreSQL and Kafka test containers from an IDE run configuration.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-kafka-tutorial
 */
public class TestSpringKafkaTutorialApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringKafkaTutorialApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
