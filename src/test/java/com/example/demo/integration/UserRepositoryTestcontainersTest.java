package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class UserRepositoryTestcontainersTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bloodbank_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                mysql::getJdbcUrl);

        registry.add(
                "spring.datasource.username",
                mysql::getUsername);

        registry.add(
                "spring.datasource.password",
                mysql::getPassword);

        registry.add(
                "spring.datasource.driver-class-name",
                mysql::getDriverClassName);

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "create-drop");

        registry.add(
                "spring.jpa.database-platform",
                () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void testDatabaseConnection() {

        assertThat(userRepository).isNotNull();
    }
}
