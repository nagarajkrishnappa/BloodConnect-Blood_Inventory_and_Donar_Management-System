package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.Donor;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.Gender;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DonorRepositoryTest {

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

        Role role = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName("USER");
                    return roleRepository.save(r);
                });

        testUser = new User();
        testUser.setFullName("Donor User");
        testUser.setEmail("donoruser@gmail.com");
        testUser.setPassword("Password@123");
        testUser.setPhone("9876543210");
        testUser.setEnabled(true);
        testUser.setRole(role);
        testUser = userRepository.save(testUser);
    }

    private Donor createDonor(User user, BloodGroup bg, String city, boolean available, boolean deleted) {
        Donor donor = new Donor();
        donor.setUser(user);
        donor.setBloodGroup(bg);
        donor.setGender(Gender.MALE);
        donor.setDateOfBirth(LocalDate.of(1995, 5, 15));
        donor.setAddress("123 Main St");
        donor.setCity(city);
        donor.setState("Karnataka");
        donor.setPincode("560001");
        donor.setWeight(70.0);
        donor.setAvailable(available);
        donor.setDeleted(deleted);
        return donor;
    }

    @Test
    void saveDonor_shouldSaveSuccessfully() {
        Donor donor = createDonor(testUser, BloodGroup.O_POSITIVE, "Bangalore", true, false);

        Donor saved = donorRepository.save(donor);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBloodGroup()).isEqualTo(BloodGroup.O_POSITIVE);
        assertThat(saved.getCity()).isEqualTo("Bangalore");
    }

    @Test
    void findByUser_shouldReturnDonor() {
        Donor donor = createDonor(testUser, BloodGroup.O_POSITIVE, "Bangalore", true, false);
        donorRepository.save(donor);

        Optional<Donor> result = donorRepository.findByUser(testUser);

        assertThat(result).isPresent();
        assertThat(result.get().getCity()).isEqualTo("Bangalore");
    }

    @Test
    void findByUserAndDeletedFalse_shouldReturnDonor_whenNotDeleted() {
        Donor donor = createDonor(testUser, BloodGroup.A_POSITIVE, "Mysore", true, false);
        donorRepository.save(donor);

        Optional<Donor> result = donorRepository.findByUserAndDeletedFalse(testUser);

        assertThat(result).isPresent();
    }

    @Test
    void findByUserAndDeletedFalse_shouldReturnEmpty_whenDeleted() {
        Donor donor = createDonor(testUser, BloodGroup.A_POSITIVE, "Mysore", true, true);
        donorRepository.save(donor);

        Optional<Donor> result = donorRepository.findByUserAndDeletedFalse(testUser);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUserAndDeletedFalse_shouldReturnTrue_whenExists() {
        Donor donor = createDonor(testUser, BloodGroup.B_POSITIVE, "Mangalore", true, false);
        donorRepository.save(donor);

        boolean exists = donorRepository.existsByUserAndDeletedFalse(testUser);

        assertThat(exists).isTrue();
    }

    @Test
    void findByAvailableTrueAndDeletedFalse_shouldReturnAvailableDonors() {
        Donor d1 = createDonor(testUser, BloodGroup.O_POSITIVE, "Bangalore", true, false);
        donorRepository.save(d1);

        List<Donor> availableList = donorRepository.findByAvailableTrueAndDeletedFalse();

        assertThat(availableList).hasSize(1);
    }

    @Test
    void findByBloodGroupAndDeletedFalse_shouldReturnMatchingBloodGroupDonors() {
        Donor d1 = createDonor(testUser, BloodGroup.AB_NEGATIVE, "Bangalore", true, false);
        donorRepository.save(d1);

        List<Donor> list = donorRepository.findByBloodGroupAndDeletedFalse(BloodGroup.AB_NEGATIVE);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getBloodGroup()).isEqualTo(BloodGroup.AB_NEGATIVE);
    }

    @Test
    void findByCityContainingIgnoreCaseAndDeletedFalse_shouldReturnMatchingCity() {
        Donor d1 = createDonor(testUser, BloodGroup.O_POSITIVE, "Bangalore", true, false);
        donorRepository.save(d1);

        List<Donor> list = donorRepository.findByCityContainingIgnoreCaseAndDeletedFalse("banga");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCity()).isEqualTo("Bangalore");
    }

    @Test
    void countByDeletedFalse_shouldReturnCorrectCount() {
        Donor d1 = createDonor(testUser, BloodGroup.O_POSITIVE, "Bangalore", true, false);
        donorRepository.save(d1);

        long count = donorRepository.countByDeletedFalse();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void searchDonors_shouldFilterByBloodGroupAndCity() {
        Donor d1 = createDonor(testUser, BloodGroup.O_POSITIVE, "Bangalore", true, false);
        donorRepository.save(d1);

        List<Donor> result = donorRepository.searchDonors(BloodGroup.O_POSITIVE, "banga");

        assertThat(result).hasSize(1);
    }

    @Test
    void findByUser_FullNameContainingIgnoreCaseAndDeletedFalse_shouldReturnMatchingPage() {
        Donor d1 = createDonor(testUser, BloodGroup.O_POSITIVE, "Bangalore", true, false);
        donorRepository.save(d1);

        Page<Donor> page = donorRepository
                .findByUser_FullNameContainingIgnoreCaseAndDeletedFalse("Donor", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
    }
}
