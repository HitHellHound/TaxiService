package com.efcon.ride;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.File;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class AbstractIntegrationTest {
    static final String KAFKA_SERVICE_NAME = "kafka";
    static final String RABBITMQ_SERVICE_NAME = "rabbitmq";
    static final String HOST_IP = DockerClientFactory.instance().dockerHostIpAddress();

    static final DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("../compose.yaml"))
                    .withServices(KAFKA_SERVICE_NAME, RABBITMQ_SERVICE_NAME)
                    .withExposedService(KAFKA_SERVICE_NAME, 9092)
                    .withExposedService(RABBITMQ_SERVICE_NAME, 5672)
                    .withEnv("HOST_IP", HOST_IP);

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        environment.start();
        postgres.start();
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", AbstractIntegrationTest::getKafkaBootstrapServers);
    }

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host",
                () -> environment.getServiceHost(RABBITMQ_SERVICE_NAME, 5672));
        registry.add("spring.rabbitmq.port",
                () -> environment.getServicePort(RABBITMQ_SERVICE_NAME, 5672));
    }

    public static String getKafkaBootstrapServers() {
        return String.format("%s:%d",
                environment.getServiceHost(KAFKA_SERVICE_NAME, 9092),
                environment.getServicePort(KAFKA_SERVICE_NAME, 9092));
    }
}
