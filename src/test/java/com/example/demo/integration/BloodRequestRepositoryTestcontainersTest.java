package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.demo.entity.BloodRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.RequestStatus;
import com.example.demo.repository.BloodRequestRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BloodRequestRepositoryTestcontainersTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bloodbank_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        bloodRequestRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("ROLE_USER");
        Role savedRole = roleRepository.save(role);

        testUser = new User();
        testUser.setFullName("Request Test User");
        testUser.setEmail("requester" + System.currentTimeMillis() + "@test.com");
        testUser.setPassword("Password@123");
        testUser.setPhone("9876543210");
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setRole(savedRole);
        testUser = userRepository.save(testUser);
    }

    @Test
    void saveBloodRequest_shouldSaveSuccessfully() {
        BloodRequest request = createBloodRequest(testUser, BloodGroup.A_POSITIVE, RequestStatus.PENDING);
        BloodRequest savedRequest = bloodRequestRepository.save(request);

        assertThat(savedRequest.getId()).isNotNull();
        assertThat(savedRequest.getBloodGroup()).isEqualTo(BloodGroup.A_POSITIVE);
        assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void findByUserAndDeletedFalse_shouldReturnRequests() {
        BloodRequest request = createBloodRequest(testUser, BloodGroup.B_POSITIVE, RequestStatus.PENDING);
        bloodRequestRepository.save(request);

        List<BloodRequest> result = bloodRequestRepository.findByUserAndDeletedFalse(testUser);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBloodGroup()).isEqualTo(BloodGroup.B_POSITIVE);
    }

    @Test
    void findByStatusAndDeletedFalse_shouldReturnMatchingStatus() {
        BloodRequest req1 = createBloodRequest(testUser, BloodGroup.O_POSITIVE, RequestStatus.PENDING);
        BloodRequest req2 = createBloodRequest(testUser, BloodGroup.AB_POSITIVE, RequestStatus.APPROVED);
        bloodRequestRepository.save(req1);
        bloodRequestRepository.save(req2);

        List<BloodRequest> pendingRequests = bloodRequestRepository.findByStatusAndDeletedFalse(RequestStatus.PENDING);
        assertThat(pendingRequests).hasSize(1);
        assertThat(pendingRequests.get(0).getBloodGroup()).isEqualTo(BloodGroup.O_POSITIVE);
    }

    @Test
    void countByDeletedFalse_shouldReturnCorrectCount() {
        BloodRequest req1 = createBloodRequest(testUser, BloodGroup.O_POSITIVE, RequestStatus.PENDING);
        BloodRequest req2 = createBloodRequest(testUser, BloodGroup.O_NEGATIVE, RequestStatus.APPROVED);
        req2.setDeleted(true);

        bloodRequestRepository.save(req1);
        bloodRequestRepository.save(req2);

        long count = bloodRequestRepository.countByDeletedFalse();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByUser_FullNameContainingIgnoreCaseAndDeletedFalse_shouldReturnMatchingPage() {
        BloodRequest request = createBloodRequest(testUser, BloodGroup.A_NEGATIVE, RequestStatus.PENDING);
        bloodRequestRepository.save(request);

        Page<BloodRequest> page = bloodRequestRepository.findByUser_FullNameContainingIgnoreCaseAndDeletedFalse("Request", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    private BloodRequest createBloodRequest(User user, BloodGroup group, RequestStatus status) {
        BloodRequest request = new BloodRequest();
        request.setUser(user);
        request.setBloodGroup(group);
        request.setUnitsRequired(2);
        request.setReason("Urgent Requirement");
        request.setStatus(status);
        request.setRequestDate(LocalDateTime.now());
        request.setDeleted(false);
        return request;
    }
}
