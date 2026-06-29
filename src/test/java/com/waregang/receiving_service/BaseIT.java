package com.waregang.receiving_service;

import com.waregang.receiving_service.integration.IntegrationTestConfig;
import com.waregang.receiving_service.security.Authority;
import com.waregang.receiving_service.security.User;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.security.UserRepository;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public abstract class BaseIT {
    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected UserPrincipal workerPrincipal;
    protected User worker;

    protected UserPrincipal managerPrincipal;
    protected User manager;


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
    void cleanDatabaseAndCreateUsers() {
        entityManager.getEntityManagerFactory()
                .unwrap(SessionFactoryImplementor.class)
                .getSchemaManager()
                .truncateMappedObjects();

        // Create Worker
        var workerRequest = new RegisterUserRequest("test_worker", "WH-001", "worker@test.com", "password");
        worker = User.createBoxCat(workerRequest, passwordEncoder.encode(workerRequest.password()));
        userRepository.save(worker);
        workerPrincipal = UserPrincipal.from(worker);

        // Create Manager
        var managerRequest = new RegisterUserRequest("test_manager", "WH-001", "manager@test.com", "password");
        manager = User.createBoxManager(managerRequest, passwordEncoder.encode(managerRequest.password()));
        userRepository.save(manager);
        managerPrincipal = UserPrincipal.from(manager);
    }
}