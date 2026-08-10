package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

import com.example.demo.entity.DonationHistory;
import com.example.demo.entity.Donor;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.Gender;
import com.example.demo.repository.DonationHistoryRepository;
import com.example.demo.repository.DonorRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

@SpringBootTest
@Testcontainers
class DonationHistoryRepositoryTestcontainersTest {

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
    }

    @Autowired
    private DonationHistoryRepository donationHistoryRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Donor testDonor;

    @BeforeEach
    void setUp() {
        donationHistoryRepository.deleteAll();
        donorRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("ROLE_USER");
        Role savedRole = roleRepository.save(role);

        User user = new User();
        user.setFullName("Donation Test Donor");
        user.setEmail("donationdonor" + System.currentTimeMillis() + "@test.com");
        user.setPassword("Password@123");
        user.setPhone("9876543210");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(savedRole);
        User savedUser = userRepository.save(user);

        Donor donor = new Donor();
        donor.setUser(savedUser);
        donor.setBloodGroup(BloodGroup.AB_POSITIVE);
        donor.setGender(Gender.FEMALE);
        donor.setDateOfBirth(LocalDate.of(1995, 6, 20));
        donor.setAddress("Donation Address");
        donor.setCity("Bengaluru");
        donor.setState("Karnataka");
        donor.setPincode("560002");
        donor.setWeight(65.0);
        donor.setAvailable(true);
        donor.setLastDonationDate(LocalDate.now());
        donor.setDeleted(false);
        testDonor = donorRepository.save(donor);
    }

    @Test
    void saveDonationHistory_shouldSaveSuccessfully() {
        DonationHistory history = createDonationHistory(testDonor, "AB+", 1);
        DonationHistory savedHistory = donationHistoryRepository.save(history);

        assertThat(savedHistory.getId()).isNotNull();
        assertThat(savedHistory.getBloodGroup()).isEqualTo("AB+");
        assertThat(savedHistory.getUnitsDonated()).isEqualTo(1);
    }

    @Test
    void findAllByDeletedFalse_shouldReturnNonDeletedHistory() {
        DonationHistory h1 = createDonationHistory(testDonor, "AB+", 1);
        DonationHistory h2 = createDonationHistory(testDonor, "AB+", 2);
        h2.setDeleted(true);

        donationHistoryRepository.save(h1);
        donationHistoryRepository.save(h2);

        List<DonationHistory> historyList = donationHistoryRepository.findAllByDeletedFalse();
        assertThat(historyList).hasSize(1);
        assertThat(historyList.get(0).getUnitsDonated()).isEqualTo(1);
    }

    @Test
    void countByDeletedFalse_shouldReturnCorrectCount() {
        DonationHistory history = createDonationHistory(testDonor, "AB+", 1);
        donationHistoryRepository.save(history);

        long count = donationHistoryRepository.countByDeletedFalse();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByDonor_User_FullNameContainingIgnoreCaseAndDeletedFalse_shouldReturnMatchingPage() {
        DonationHistory history = createDonationHistory(testDonor, "AB+", 1);
        donationHistoryRepository.save(history);

        Page<DonationHistory> page = donationHistoryRepository
                .findByDonor_User_FullNameContainingIgnoreCaseAndDeletedFalse("Donation", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getDonor().getUser().getFullName()).contains("Donation");
    }

    private DonationHistory createDonationHistory(Donor donor, String bloodGroupStr, int units) {
        DonationHistory history = new DonationHistory();
        history.setDonor(donor);
        history.setDonationDate(LocalDate.now());
        history.setBloodGroup(bloodGroupStr);
        history.setUnitsDonated(units);
        history.setRemarks("Regular Donation");
        history.setDeleted(false);
        return history;
    }
}
