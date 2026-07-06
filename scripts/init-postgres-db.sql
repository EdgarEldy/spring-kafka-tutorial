-- One time setup: creates a dedicated database and role for spring-kafka-tutorial inside
-- an existing Postgres 16 instance, so you do not have to run a second Postgres container
-- just for this project if you already have one available.
-- Usage (adapt the container name and superuser to your own setup):
--   docker exec -i <your-postgres-container> psql -U <superuser> -d <default-database> < scripts/init-postgres-db.sql

CREATE DATABASE spring_kafka_tutorial;
CREATE USER spring_kafka_tutorial WITH PASSWORD 'spring_kafka_tutorial';
GRANT ALL PRIVILEGES ON DATABASE spring_kafka_tutorial TO spring_kafka_tutorial;
