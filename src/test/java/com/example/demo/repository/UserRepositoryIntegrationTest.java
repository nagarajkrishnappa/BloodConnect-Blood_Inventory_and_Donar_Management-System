package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userRole = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("USER");
                    return roleRepository.save(role);
                });
    }

    // =====================================================
    // SAVE USER
    // =====================================================

    @Test
    void saveUser_shouldSaveUserSuccessfully() {

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");
        user.setPassword("Password@123");
        user.setPhone("9876543210");
        user.setEnabled(true);
        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFullName())
                .isEqualTo("Nagaraja");

        assertThat(savedUser.getEmail())
                .isEqualTo("nagaraja@gmail.com");
    }

    // =====================================================
    // FIND BY EMAIL
    // =====================================================

    @Test
    void findByEmail_shouldReturnUser() {

        User user = createUser(
                "Nagaraja",
                "nagaraja@gmail.com",
                "9876543210");

        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("nagaraja@gmail.com");

        assertThat(result).isPresent();

        assertThat(result.get().getFullName())
                .isEqualTo("Nagaraja");

        assertThat(result.get().getEmail())
                .isEqualTo("nagaraja@gmail.com");
    }

    // =====================================================
    // FIND BY EMAIL - NOT FOUND
    // =====================================================

    @Test
    void findByEmail_shouldReturnEmpty_whenUserDoesNotExist() {

        Optional<User> result = userRepository.findByEmail(
                "unknown@gmail.com");

        assertThat(result).isEmpty();
    }

    // =====================================================
    // EXISTS BY EMAIL
    // =====================================================

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {

        User user = createUser(
                "Nagaraja",
                "nagaraja@gmail.com",
                "9876543210");

        userRepository.save(user);

        boolean exists = userRepository.existsByEmail(
                "nagaraja@gmail.com");

        assertThat(exists).isTrue();
    }

    // =====================================================
    // EXISTS BY EMAIL - FALSE
    // =====================================================

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {

        boolean exists = userRepository.existsByEmail(
                "unknown@gmail.com");

        assertThat(exists).isFalse();
    }

    // =====================================================
    // FIND BY FULL NAME
    // =====================================================

    @Test
    void findByFullNameContainingIgnoreCase_shouldReturnMatchingUsers() {

        userRepository.save(
                createUser(
                        "Nagaraja",
                        "nagaraja@gmail.com",
                        "9876543210"));

        userRepository.save(
                createUser(
                        "Nagaraj Kumar",
                        "nagarajkumar@gmail.com",
                        "9876543211"));

        List<User> users = userRepository
                .findByFullNameContainingIgnoreCase(
                        "nagar");

        assertThat(users).hasSize(2);
    }

    // =====================================================
    // FIND BY EMAIL CONTAINING
    // =====================================================

    @Test
    void findByEmailContainingIgnoreCase_shouldReturnMatchingUsers() {

        userRepository.save(
                createUser(
                        "Nagaraja",
                        "nagaraja@gmail.com",
                        "9876543210"));

        userRepository.save(
                createUser(
                        "Rahul",
                        "rahul@yahoo.com",
                        "9876543211"));

        List<User> users = userRepository
                .findByEmailContainingIgnoreCase(
                        "gmail");

        assertThat(users).hasSize(1);

        assertThat(users.get(0).getEmail())
                .isEqualTo("nagaraja@gmail.com");
    }

    // =====================================================
    // FIND BY PHONE
    // =====================================================

    @Test
    void findByPhoneContaining_shouldReturnMatchingUsers() {

        userRepository.save(
                createUser(
                        "Nagaraja",
                        "nagaraja@gmail.com",
                        "9876543210"));

        userRepository.save(
                createUser(
                        "Rahul",
                        "rahul@gmail.com",
                        "9876543211"));

        List<User> users = userRepository
                .findByPhoneContaining("987654");

        assertThat(users).hasSize(2);
    }

    // =====================================================
    // SEARCH BY NAME OR EMAIL
    // =====================================================

    @Test
    void findByFullNameOrEmail_shouldReturnMatchingUsers() {

        userRepository.save(
                createUser(
                        "Nagaraja",
                        "nagaraja@gmail.com",
                        "9876543210"));

        userRepository.save(
                createUser(
                        "Rahul",
                        "rahul@yahoo.com",
                        "9876543211"));

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        "nagar",
                        "nagar",
                        pageable);

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent()
                .get(0)
                .getFullName())
                .isEqualTo("Nagaraja");
    }

    // =====================================================
    // PAGINATION
    // =====================================================

    @Test
    void findAll_shouldSupportPagination() {

        userRepository.save(
                createUser(
                        "Nagaraja",
                        "nagaraja@gmail.com",
                        "9876543210"));

        userRepository.save(
                createUser(
                        "Rahul",
                        "rahul@gmail.com",
                        "9876543211"));

        userRepository.save(
                createUser(
                        "Kiran",
                        "kiran@gmail.com",
                        "9876543212"));

        Pageable pageable = PageRequest.of(0, 2);

        Page<User> result = userRepository.findAll(pageable);

        assertThat(result.getContent())
                .hasSize(2);

        assertThat(result.getTotalElements())
                .isEqualTo(3);

        assertThat(result.getTotalPages())
                .isEqualTo(2);
    }

    // =====================================================
    // ENTITY GRAPH - ROLE
    // =====================================================

    @Test
    void findByEmail_shouldLoadRole() {

        User user = createUser(
                "Nagaraja",
                "nagaraja@gmail.com",
                "9876543210");

        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail(
                "nagaraja@gmail.com");

        assertThat(result).isPresent();

        assertThat(result.get().getRole())
                .isNotNull();

        assertThat(result.get().getRole().getRoleName())
                .isEqualTo("USER");
    }

    // =====================================================
    // HELPER METHOD
    // =====================================================

    private User createUser(
            String fullName,
            String email,
            String phone) {

        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword("Password@123");
        user.setPhone(phone);
        user.setEnabled(true);
        user.setRole(userRole);

        return user;
    }
}