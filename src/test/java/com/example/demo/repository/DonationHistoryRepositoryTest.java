package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.DonationHistory;
import com.example.demo.entity.Donor;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.Gender;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DonationHistoryRepositoryTest {

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

        Role role = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName("USER");
                    return roleRepository.save(r);
                });

        User user = new User();
        user.setFullName("History User");
        user.setEmail("historyuser@gmail.com");
        user.setPassword("Password@123");
        user.setPhone("9876543210");
        user.setEnabled(true);
        user.setRole(role);
        user = userRepository.save(user);

        testDonor = new Donor();
        testDonor.setUser(user);
        testDonor.setBloodGroup(BloodGroup.O_POSITIVE);
        testDonor.setGender(Gender.MALE);
        testDonor.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testDonor.setAddress("456 Park Ave");
        testDonor.setCity("Bangalore");
        testDonor.setState("Karnataka");
        testDonor.setPincode("560002");
        testDonor.setWeight(75.0);
        testDonor.setAvailable(true);
        testDonor.setDeleted(false);
        testDonor = donorRepository.save(testDonor);
    }

    private DonationHistory createHistory(Donor donor, String bg, int units, boolean deleted) {
        DonationHistory history = new DonationHistory();
        history.setDonor(donor);
        history.setDonationDate(LocalDate.now());
        history.setBloodGroup(bg);
        history.setUnitsDonated(units);
        history.setRemarks("Routine donation");
        history.setDeleted(deleted);
        return history;
    }

    @Test
    void saveDonationHistory_shouldSaveSuccessfully() {
        DonationHistory history = createHistory(testDonor, "O_POSITIVE", 1, false);

        DonationHistory saved = donationHistoryRepository.save(history);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBloodGroup()).isEqualTo("O_POSITIVE");
        assertThat(saved.getUnitsDonated()).isEqualTo(1);
    }

    @Test
    void findAllByDeletedFalse_shouldReturnActiveRecords() {
        DonationHistory dh1 = createHistory(testDonor, "O_POSITIVE", 1, false);
        DonationHistory dh2 = createHistory(testDonor, "A_POSITIVE", 2, true);
        donationHistoryRepository.saveAll(List.of(dh1, dh2));

        List<DonationHistory> activeList = donationHistoryRepository.findAllByDeletedFalse();

        assertThat(activeList).hasSize(1);
        assertThat(activeList.get(0).getBloodGroup()).isEqualTo("O_POSITIVE");
    }

    @Test
    void countByDeletedFalse_shouldReturnCorrectCount() {
        DonationHistory dh1 = createHistory(testDonor, "O_POSITIVE", 1, false);
        DonationHistory dh2 = createHistory(testDonor, "B_POSITIVE", 1, false);
        DonationHistory dh3 = createHistory(testDonor, "A_POSITIVE", 2, true);
        donationHistoryRepository.saveAll(List.of(dh1, dh2, dh3));

        long count = donationHistoryRepository.countByDeletedFalse();

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void findByDonor_User_FullNameContainingIgnoreCaseAndDeletedFalse_shouldReturnMatchingPage() {
        DonationHistory dh1 = createHistory(testDonor, "O_POSITIVE", 1, false);
        donationHistoryRepository.save(dh1);

        Page<DonationHistory> page = donationHistoryRepository
                .findByDonor_User_FullNameContainingIgnoreCaseAndDeletedFalse("History", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getDonor().getUser().getFullName()).isEqualTo("History User");
    }
}
