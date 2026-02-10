package com.efcon.rating;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MongoDBContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
    @ServiceConnection
    private static final MongoDBContainer mongodb = new MongoDBContainer("mongo:latest");

    private static final WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:latest")
            .withCliArg("--global-response-templating")
            .withExtensions("Grpc&Faker",
                    List.of( "org.wiremock.RandomExtension"),
                    getWiremockJars())
            .withMappingFromResource("wiremock/ride.json")
            .withFileSystemBind("target/generated-resources/protobuf/descriptor-sets",
                    "/home/wiremock/grpc",
                    BindMode.READ_ONLY);

    static {
        mongodb.start();
        wiremock.start();
    }

    @DynamicPropertySource
    static void wiremockProperties(DynamicPropertyRegistry registry) {
        String grpcAddress = "static://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080);
        registry.add("grpc.client.ride-service.address", () -> grpcAddress);
    }

    @DynamicPropertySource
    static void mongodbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }

    private static List<File> getWiremockJars() {
        Path dir = Paths.get("target", "test-wiremock-extension");
        try {
            return Files.list(dir).map(Path::toFile).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
