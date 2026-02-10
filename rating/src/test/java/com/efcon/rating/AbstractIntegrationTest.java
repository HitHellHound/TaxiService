package com.efcon.rating;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
    @ServiceConnection
    private static final MongoDBContainer mongodb = new MongoDBContainer("mongo:latest");

    private static final WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:latest")
            .withCliArg("--global-response-templating");

    static {
        mongodb.start();
        wiremock.start();
    }

    @DynamicPropertySource
    static void wiremockProperties(DynamicPropertyRegistry registry) {
        registry.add("grpc.client.ride-service.address", wiremock::getBaseUrl);
    }

    @DynamicPropertySource
    static void mongodbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }
}
