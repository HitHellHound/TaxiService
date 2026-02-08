package com.efcon.ride;

import com.efcon.ride.service.RideNotificationService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class AbstractIntegrationTest {
    static final String KAFKA_SERVICE_NAME = "kafka";
    static final String RABBITMQ_SERVICE_NAME = "rabbitmq";
    static final String HOST_IP = DockerClientFactory.instance().dockerHostIpAddress();

    @MockitoSpyBean
    private RideNotificationService rideNotificationServiceSpy;

    static final DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("../compose.yaml"))
                    .withServices(KAFKA_SERVICE_NAME, RABBITMQ_SERVICE_NAME)
                    .withExposedService(KAFKA_SERVICE_NAME, 9092)
                    .withExposedService(RABBITMQ_SERVICE_NAME, 5672)
                    .withEnv("HOST_IP", HOST_IP);

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    static final WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:latest")
            .withCliArg("--global-response-templating")
            .withExtensions("Faker",
                    Collections.singleton("org.wiremock.RandomExtension"),
                    getWiremockJars())
            .withMappingFromResource("wiremock/passenger.json")
            .withFileFromResource("wiremock/responses/get-all-passengers.json.hbs");

    static {
        environment.start();
        postgres.start();
        wiremock.start();
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

    @DynamicPropertySource
    static void wiremockProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.discovery.client.simple.instances.passenger[0].uri", wiremock::getBaseUrl);
    }

    public static String getKafkaBootstrapServers() {
        return String.format("%s:%d",
                environment.getServiceHost(KAFKA_SERVICE_NAME, 9092),
                environment.getServicePort(KAFKA_SERVICE_NAME, 9092));
    }

    private static List<File> getWiremockJars() {
        Path dir = Paths.get("target", "test-wiremock-extension");
        try {
            return Files.list(dir).map(Path::toFile).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected RideNotificationService getRideNotificationServiceSpy() {
        return this.rideNotificationServiceSpy;
    }
}
