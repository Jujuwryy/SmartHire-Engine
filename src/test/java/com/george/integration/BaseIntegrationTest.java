package com.george.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
    "app.embeddings.huggingface.access-token=test-token",
    "app.embeddings.huggingface.model-id=test-model",
    "app.security.rate-limit.generate.requests=10",
    "app.security.rate-limit.generate.window-minutes=1",
    "app.security.rate-limit.match.requests=100",
    "app.security.rate-limit.match.window-minutes=1",
    "app.security.actuator.allowed-ips=127.0.0.1,::1",
    "SWAGGER_ENABLED=true"
})
public abstract class BaseIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("app.mongodb.database-name", () -> "test_db");
        registry.add("app.mongodb.collection-name", () -> "JobPost");
    }
}

