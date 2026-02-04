package com.efcon.driver;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.File;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {
    static final String KAFKA_SERVICE_NAME = "kafka";
    static final String HOST_IP = DockerClientFactory.instance().dockerHostIpAddress();
    static String KAFKA_BOOTSTRAP_SERVERS;

    static final DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("../compose.yaml"))
                    .withServices(KAFKA_SERVICE_NAME)
                    .withExposedService(KAFKA_SERVICE_NAME, 9092)
                    .withEnv("HOST_IP", HOST_IP);

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        environment.start();
        postgres.start();
        KAFKA_BOOTSTRAP_SERVERS = String.format("%s:%d",
                environment.getServiceHost(KAFKA_SERVICE_NAME, 9092),
                environment.getServicePort(KAFKA_SERVICE_NAME, 9092));
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
         registry.add("spring.kafka.bootstrap-servers", AbstractIntegrationTest::getKafkaBootstrapServers);
    }

    public static String getKafkaBootstrapServers() {
        return KAFKA_BOOTSTRAP_SERVERS;
    }
}
