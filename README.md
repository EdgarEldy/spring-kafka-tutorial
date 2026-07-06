# spring-kafka-tutorial

A complete, hands-on walkthrough of integrating **Kafka** into a **Spring Boot 3.5.x** (Spring Framework 6, Java 17) application, organized into Git branches to cover the essential concepts of event-driven messaging with Spring Kafka.

The data model follows the `spring-boot-tutorial` tutorial: `categories` → `products` → `orders` ← `customers`.

This document is the **complete specification** of the project: it is meant to be followed step by step to implement each branch.

## Table of contents

- [Business scenario](#business-scenario)
- [Kafka fundamentals](#kafka-fundamentals)
- [Tech stack](#tech-stack)
- [Data model](#data-model)
- [Branching strategy](#branching-strategy)
- [Project structure](#project-structure)
- [Standard response format](#standard-response-format)
- [feature/core-architecture](#featurecore-architecture)
- [feature/products](#featureproducts)
- [feature/customers](#featurecustomers)
- [feature/orders](#featureorders)
- [feature/messaging](#featuremessaging)
- [feature/schema-registry (bonus)](#featureschema-registry-bonus)
- [Order of work](#order-of-work)
- [Code conventions](#code-conventions)
- [Concepts covered](#concepts-covered)
- [How to follow this tutorial](#how-to-follow-this-tutorial)

## Business scenario

When an order (`Order`) is created through the API, an `OrderCreatedEvent` is **published asynchronously** to a Kafka topic. A separate consumer reads this event and **decrements the stock** of the corresponding product. This decoupling between "create the order" and "process the impact on stock" is the classic use case that justifies going asynchronous: order creation responds to the user immediately, without waiting for the stock update.

## Kafka fundamentals

Explaining how Kafka actually works, not just how to annotate a producer/consumer, is the actual goal of this tutorial. Kafka is a distributed, append-only commit log used as a message broker; a handful of core concepts are essential before touching the code in [feature/messaging](#featuremessaging):

- **Topics and partitions**: a topic (`order-events` here) is split into partitions, each an ordered, immutable, append-only log. Order is only guaranteed **within** a single partition, never across the partitions of a topic.
- **Partitioning key**: `OrderEventProducer` publishes with `productId` as the record key; Kafka hashes the key to always route events for the same product to the same partition. That is precisely what guarantees per-product ordering, not any property of the topic as a whole.
- **Producers and delivery guarantees**: producers append records to the log; how durable that write is depends on the acknowledgment setting (`acks=all` waits for all in-sync replicas) and on whether the producer is configured to be idempotent (safe to retry a send without duplicating the record).
- **Consumers, consumer groups, and offsets**: a consumer tracks its read position in a partition with an **offset**. Consumers that share a `group-id` split a topic's partitions between them so each partition is actively read by only one consumer per group at a time - which means **the number of partitions caps how many consumers in a group can work in parallel** (see the partition-count task in [feature/messaging](#featuremessaging)).
- **Delivery semantics**: this tutorial uses **at-least-once** delivery, the default and most common choice: a message can be redelivered and reprocessed after a consumer crashes before committing its offset. In practice this means `StockService.decrementStock` must tolerate being called more than once for the same event; true exactly-once processing needs additional idempotency work that this tutorial calls out but does not fully implement.
- **Error handling: retry and dead-letter topic**: a message that keeps failing (bad data, a downstream outage) should not block its partition forever. `DefaultErrorHandler` retries a bounded number of times with backoff, then routes the message to a **dead-letter topic** (`order-events-dlt`) so the rest of the partition keeps flowing while the failure is dealt with separately.
- **KRaft instead of Zookeeper**: Kafka no longer needs a separate Zookeeper cluster to store cluster metadata (controller election, topic configuration); this project runs Kafka in **KRaft** mode, where a subset of the brokers themselves form the metadata quorum.
- **Schema evolution** (bonus branch): once multiple services read and write the same topic, changing the message format becomes risky. A **Schema Registry** (Avro) lets producers and consumers agree on compatible schema changes (e.g. adding an optional field) while rejecting breaking ones (e.g. removing a required field) before they ever reach production.

## Tech stack

| Component | Choice |
|---|---|
| Framework | Spring Boot 3.5.x (Spring Framework 6) |
| Language | Java 17 (LTS) |
| Build | Maven |
| Database | PostgreSQL 16 (via Docker Compose) |
| Message broker | Apache Kafka (KRaft, no Zookeeper) |
| Kafka client | Spring Kafka (`spring-kafka`) |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| DTO mapping | MapStruct + Lombok |
| Validation | Jakarta Bean Validation |
| API documentation | springdoc-openapi (Swagger UI) |
| Monitoring | Spring Boot Actuator |
| Tests | JUnit 5, Mockito, Testcontainers (PostgreSQL + Kafka) |
| CI/CD | GitHub Actions |
| Containerization | Docker, docker-compose |

## Data model

```
categories (id, category_name)
    │ 1
    │
    │ N
products (id, category_id, product_name, unit_price, stock_quantity)
    │ 1
    │
    │ N
orders (id, customer_id, product_id, quantity, total)
    │ N
    │
    │ 1
customers (id, first_name, last_name, telephone, email, address)
```

Columns and constraints are identical to `spring-boot-tutorial`, with one addition: **`products.stock_quantity`** (INT, NOT NULL, ≥ 0), needed to illustrate the stock-decrement scenario triggered by Kafka.

## Branching strategy

| Branch | Role |
|---|---|
| `master` | Stable, production-ready code. No direct commits, only merges from `develop`. |
| `develop` | Integration branch. |
| `feature/core-architecture` | Technical foundation: project structure, configuration, Docker (app + PostgreSQL + Kafka), CI. |
| `feature/products` | `Category`/`Product` CRUD (with `stockQuantity`). |
| `feature/customers` | `Customer` CRUD. |
| `feature/orders` | `Order` CRUD, no messaging yet (computes `total`). |
| `feature/messaging` | Kafka producer and consumer: publishing `OrderCreatedEvent`, stock decrement, error handling (retry, DLT). |
| `feature/schema-registry` | *Bonus*: migrating JSON serialization to Avro + Schema Registry. |

## Project structure

```
spring-kafka-tutorial/
├── src/
│   ├── main/
│   │   ├── java/edgareldy/springkafkatutorial/
│   │   │   ├── SpringKafkaTutorialApplication.java
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── KafkaProducerConfig.java
│   │   │   │   ├── KafkaConsumerConfig.java
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   └── CorsConfig.java
│   │   │   ├── entity/
│   │   │   │   ├── Category.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── Customer.java
│   │   │   │   └── Order.java
│   │   │   ├── repository/
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   └── OrderRepository.java
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java
│   │   │   │   │   └── PageResponse.java
│   │   │   │   ├── category/ (CategoryRequest, CategoryResponse)
│   │   │   │   ├── product/ (ProductRequest, ProductResponse)
│   │   │   │   ├── customer/ (CustomerRequest, CustomerResponse)
│   │   │   │   ├── order/ (OrderRequest, OrderResponse)
│   │   │   │   └── event/
│   │   │   │       └── OrderCreatedEvent.java   (payload published to Kafka, shared by producer/consumer)
│   │   │   ├── mapper/
│   │   │   │   ├── CategoryMapper.java
│   │   │   │   ├── ProductMapper.java
│   │   │   │   ├── CustomerMapper.java
│   │   │   │   └── OrderMapper.java
│   │   │   ├── service/
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── StockService.java
│   │   │   │   └── impl/
│   │   │   │       ├── CategoryServiceImpl.java
│   │   │   │       ├── ProductServiceImpl.java
│   │   │   │       ├── CustomerServiceImpl.java
│   │   │   │       ├── OrderServiceImpl.java
│   │   │   │       └── StockServiceImpl.java
│   │   │   ├── controller/
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   └── OrderController.java
│   │   │   ├── messaging/
│   │   │   │   ├── producer/
│   │   │   │   │   └── OrderEventProducer.java
│   │   │   │   └── consumer/
│   │   │   │       ├── OrderEventConsumer.java
│   │   │   │       └── OrderEventDltConsumer.java   (dead-letter topic consumer)
│   │   │   └── exception/
│   │   │       ├── ResourceNotFoundException.java
│   │   │       ├── BusinessRuleException.java
│   │   │       ├── ErrorResponse.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           └── V1__init_schema.sql
│   └── test/
│       └── java/edgareldy/springkafkatutorial/
│           ├── controller/ (MockMvc)
│           ├── service/ (Mockito)
│           ├── messaging/ (producer/consumer tests with EmbeddedKafka or Testcontainers Kafka)
│           └── repository/ (@DataJpaTest)
├── docker-compose.yml       (app + PostgreSQL + Kafka KRaft + Kafka UI)
├── Dockerfile
├── .github/workflows/ci.yml
├── pom.xml
└── README.md
```

## Standard response format

Same principle as the other tutorials in the series: every HTTP response is wrapped in a generic `ApiResponse<T>` (`dto/common/ApiResponse.java`), with `PageResponse<T>` for paginated lists. See the `spring-boot-tutorial` README for the full contract.

## feature/core-architecture

Technical foundation shared by the whole project, to be merged first into `develop`. Originally named `feature/config`; renamed to better reflect that it lays out the whole architectural skeleton (config, Docker, CI), not just configuration files.

### Tasks

- [x] Initialize the project via Spring Initializr (Maven, Java 17, Spring Boot 3.5.16)
- [x] Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`, `spring-kafka`, `flyway-core`, `flyway-database-postgresql`, `postgresql` driver, `lombok`, `mapstruct` + `mapstruct-processor`, `springdoc-openapi-starter-webmvc-ui`
- [x] Test dependencies: `spring-boot-starter-test`, `spring-boot-testcontainers`, `spring-kafka-test` (for `EmbeddedKafka`), `testcontainers` (junit-jupiter, postgresql, kafka)
- [x] Package tree shown above
- [ ] `application.yml`/`application-dev.yml`: datasource, Kafka config (`bootstrap-servers`, default serializers)
- [ ] Flyway script `V1__init_schema.sql` (tables + `stock_quantity` column on `products`)
- [ ] `GlobalExceptionHandler`, `ApiResponse<T>`, `PageResponse<T>`
- [ ] `docker-compose.yml`: PostgreSQL, Kafka in KRaft mode (no Zookeeper), and **Kafka UI** (web interface to inspect topics/messages/consumer groups)
- [ ] `.github/workflows/ci.yml` (build + tests, with a Kafka service for integration tests)

### Configuration notes

- **Spring Boot 3.5.16, not 4.1.x.** The project was first scoped around Spring Boot 4.1 /
  Spring Framework 7, but springdoc-openapi had no release compatible with Spring Framework 7
  on Maven Central at implementation time (the same blocker already hit on
  `spring-boot-tutorial`). Spring Boot 3.5.16 (Spring Framework 6) was chosen directly instead
  of re-checking compatibility, on the reasoning that the sibling project already validated
  this combination of versions.
- **`dependencyManagement` explicitly imports `spring-boot-dependencies`** as
  `${project.parent.version}`, in addition to inheriting it via `<parent>`. This is redundant
  (the parent POM already provides the same bill of materials) but makes version management
  visible directly in `dependencyManagement`, matching `spring-boot-tutorial`.
- **Compiling requires Java 17 explicitly.** The default `JAVA_HOME` on the development
  machine points to Java 8, which fails with a "class file has wrong version" error against
  Spring Boot 3.5.16. Run Maven with
  `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./mvnw ...`.

## feature/products

Classic `Category`/`Product` CRUD, with the `stockQuantity` field on `Product` (needed for `feature/messaging`).

### Tasks

- [ ] Entities, repositories, DTOs, mappers
- [ ] `CategoryService`/`ProductService` interfaces + implementations
- [ ] REST controllers, pagination, filtering by `categoryId`
- [ ] Unit and integration tests

## feature/customers

Classic `Customer` CRUD (identical to the other tutorials in the series).

### Tasks

- [ ] Entity, repository, DTOs, mapper
- [ ] `CustomerService` interface + implementation
- [ ] REST controller
- [ ] Tests

## feature/orders

`Order` CRUD **without messaging yet** - the Kafka trigger arrives in the next branch.

### Tasks

- [ ] `Order` entity, repository with joins
- [ ] DTOs, mapper
- [ ] `OrderService` interface + implementation: computes `total`, checks that the product has enough stock (`BusinessRuleException` otherwise)
- [ ] REST controller
- [ ] Tests

## feature/messaging

The core of the tutorial: Kafka integration, producer and consumer.

### Tasks

- [ ] `KafkaProducerConfig`: `ProducerFactory<String, OrderCreatedEvent>`, `KafkaTemplate`, `JsonSerializer`
- [ ] `KafkaConsumerConfig`: `ConsumerFactory`, `ConcurrentKafkaListenerContainerFactory`, `JsonDeserializer` (with `TRUSTED_PACKAGES` configured), dedicated `group-id`
- [ ] `OrderEventProducer`: publishes an `OrderCreatedEvent` (orderId, productId, quantity) to the `order-events` topic, **partitioning key = `productId`** (guarantees that all events for a given product are processed in order by the same partition)
- [ ] `OrderServiceImpl`: after an order is successfully created, calls `OrderEventProducer.publish(...)` (ideally after the transaction commits, via `TransactionSynchronizationManager` or `@TransactionalEventListener`)
- [ ] `OrderEventConsumer` (`@KafkaListener` on `order-events`): calls `StockService.decrementStock(productId, quantity)`
- [ ] `StockService` interface + `StockServiceImpl` implementation: decrements `stock_quantity`, throws a business exception if stock would go negative
- [ ] Consumption error handling: `DefaultErrorHandler` with **retry** (e.g. 3 attempts, exponential backoff) then routing to a **dead-letter topic** (`order-events-dlt`) via `DeadLetterPublishingRecoverer`
- [ ] `OrderEventDltConsumer`: consumes the DLT, logs the final failure (basis for an alert or manual handling)
- [ ] Configuring the **number of partitions** of the `order-events` topic (e.g. 3) and explaining the impact on consumption parallelism
- [ ] Tests: `EmbeddedKafka` for fast in-memory tests, Testcontainers Kafka for tests closer to production; verify publishing, consumption, and DLT behavior on a simulated exception

## feature/schema-registry (bonus)

Optional branch, to be done only once `feature/messaging` is stable. Replaces JSON serialization with **Avro + Confluent Schema Registry**.

### Tasks

- [ ] Add the `schema-registry` service to `docker-compose.yml`
- [ ] Dependencies: `kafka-avro-serializer`, `avro`
- [ ] Schema file `order-created-event.avsc` defining `OrderCreatedEvent`
- [ ] `avro-maven-plugin` to generate the Java class from the `.avsc` file
- [ ] Reconfigure the producer/consumer to use `KafkaAvroSerializer`/`KafkaAvroDeserializer`
- [ ] Demonstrate a compatible schema evolution (adding an optional field) and an incompatible one (removing a required field, rejected by the Schema Registry)
- [ ] Compare JSON vs Avro message size in the branch README

## Order of work

1. `feature/core-architecture` → Pull Request to `develop`
2. `feature/products` (depends on `config`) → Pull Request to `develop`
3. `feature/customers` (depends on `config`) → Pull Request to `develop`
4. `feature/orders` (depends on `products` and `customers`) → Pull Request to `develop`
5. `feature/messaging` (depends on `orders`) → Pull Request to `develop`
6. `feature/schema-registry` (bonus, depends on `messaging`) → Pull Request to `develop`
7. `develop` → `master`

## Code conventions

- Root package: `edgareldy.springkafkatutorial`
- DTOs: Java `record`
- **Contract/implementation services**: interface at the root of `service/`, implementation in `service/impl/`
- Every controller returns an `ApiResponse<T>`
- The Kafka payload (`OrderCreatedEvent`) is an independent DTO, never the `Order` JPA entity itself
- Any Kafka publication triggered by a database write happens **after the transaction commits**, never before (to avoid publishing an event whose underlying data does not exist yet if the transaction fails)

## Concepts covered

- Layered architecture (controller / service / repository)
- Spring Data JPA, DTOs, MapStruct, validation, centralized exception handling
- Kafka producer (`KafkaTemplate`, JSON serialization)
- Kafka consumer (`@KafkaListener`, JSON deserialization, `group-id`)
- Partitioning and partitioning key
- Consumer groups and parallelism
- Consumption error handling: retry, backoff, dead-letter topic
- Transactional consistency between database write and event publication
- *(Bonus)* Avro and Schema Registry, schema evolution and compatibility
- Messaging tests (`EmbeddedKafka`, Testcontainers Kafka)
- API documentation (OpenAPI / Swagger UI)
- Observability (Actuator)
- Containerization (Docker, docker-compose, Kafka UI)
- Continuous integration (GitHub Actions)

## How to follow this tutorial

1. Clone the repository and check out `develop`
2. Follow the branches in order: `feature/core-architecture` → `feature/products` → `feature/customers` → `feature/orders` → `feature/messaging` → (bonus) `feature/schema-registry`
3. Run `docker-compose up` (PostgreSQL + Kafka + Kafka UI)
4. Open Swagger UI at `http://localhost:8080/swagger-ui.html` and Kafka UI at `http://localhost:8090` to watch topics and messages in real time
