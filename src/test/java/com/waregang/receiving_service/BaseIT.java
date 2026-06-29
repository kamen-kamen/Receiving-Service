package com.waregang.receiving_service;

import com.waregang.receiving_service.integration.IntegrationTestConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public abstract class BaseIT {
    @PersistenceContext
    protected EntityManager entityManager;

    @Container
    @ServiceConnection
    public static final PostgreSQLContainer postgreSQL = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine")
    );

//    @Container
//    @ServiceConnection
//    public static final RedisContainer redis = new RedisContainer(
//            DockerImageName.parse("redis:7.2-alpine")
//    );

    @Container
    @ServiceConnection
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka:3.7.0")
    );

    @BeforeEach
    void cleanDatabase() {
        entityManager.getEntityManagerFactory()
                .unwrap(SessionFactoryImplementor.class)
                .getSchemaManager()
                .truncateMappedObjects();
    }
}
