package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.demo.entity.Donor;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.Gender;
import com.example.demo.repository.DonorRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class DonorRepositoryTestcontainersTest {

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
    private DonorRepository donorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;

    @BeforeEach
    void setUp() {

        donorRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();

        role.setRoleName("ROLE_USER");

        Role savedRole = roleRepository.save(role);

        testUser = new User();

        testUser.setFullName("Test Donor");
        testUser.setEmail(
                "donor" + System.currentTimeMillis()
                        + "@test.com");
        testUser.setPassword("Password@123");
        testUser.setPhone("9876543210");
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setRole(savedRole);

        testUser = userRepository.save(testUser);
    }

    // =========================================================
    // SAVE DONOR
    // =========================================================

    @Test
    void saveDonor_shouldSaveSuccessfully() {

        Donor donor = createDonor();

        Donor savedDonor = donorRepository.save(donor);

        assertThat(savedDonor.getId())
                .isNotNull();

        assertThat(savedDonor.getBloodGroup())
                .isEqualTo(BloodGroup.O_POSITIVE);

        assertThat(savedDonor.getGender())
                .isEqualTo(Gender.MALE);

        assertThat(savedDonor.getCity())
                .isEqualTo("Bengaluru");
    }

    // =========================================================
    // FIND DONOR BY ID
    // =========================================================

    @Test
    void findById_shouldReturnDonor() {

        Donor donor = createDonor();

        Donor savedDonor = donorRepository.save(donor);

        Optional<Donor> result = donorRepository.findById(
                savedDonor.getId());

        assertThat(result)
                .isPresent();

        assertThat(result.get().getCity())
                .isEqualTo("Bengaluru");
    }

    // =========================================================
    // FIND ALL DONORS
    // =========================================================

    @Test
    void findAll_shouldReturnDonors() {

        Donor donor1 = createDonor();

        User user2 = new User();
        user2.setFullName("Test Donor 2");
        user2.setEmail("donor2" + System.currentTimeMillis() + "@test.com");
        user2.setPassword("Password@123");
        user2.setPhone("9876543211");
        user2.setEnabled(true);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setRole(testUser.getRole());
        User savedUser2 = userRepository.save(user2);

        Donor donor2 = createDonor();
        donor2.setUser(savedUser2);

        donorRepository.save(donor1);
        donorRepository.save(donor2);

        assertThat(
                donorRepository.findAll())
                .hasSize(2);
    }

    // =========================================================
    // DELETE DONOR
    // =========================================================

    @Test
    void delete_shouldRemoveDonor() {

        Donor donor = donorRepository.save(
                createDonor());

        Long donorId = donor.getId();

        donorRepository.deleteById(
                donorId);

        Optional<Donor> result = donorRepository.findById(
                donorId);

        assertThat(result)
                .isEmpty();
    }

    // =========================================================
    // HELPER METHOD
    // =========================================================

    private Donor createDonor() {

        Donor donor = new Donor();

        donor.setUser(testUser);

        donor.setBloodGroup(
                BloodGroup.O_POSITIVE);

        donor.setGender(
                Gender.MALE);

        donor.setDateOfBirth(
                LocalDate.of(
                        1998,
                        5,
                        15));

        donor.setAddress(
                "Test Address");

        donor.setCity(
                "Bengaluru");

        donor.setState(
                "Karnataka");

        donor.setPincode(
                "560001");

        donor.setWeight(
                70.0);

        donor.setAvailable(true);

        donor.setLastDonationDate(
                LocalDate.of(
                        2026,
                        1,
                        10));

        donor.setDeleted(false);

        return donor;
    }
}