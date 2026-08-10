package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.Role;

@DataJpaTest
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
    }

    @Test
    void saveRole_shouldSaveRoleSuccessfully() {
        Role role = new Role();
        role.setRoleName("ROLE_USER");

        Role savedRole = roleRepository.save(role);

        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getRoleName()).isEqualTo("ROLE_USER");
    }

    @Test
    void findByRoleName_shouldReturnRole_whenRoleExists() {
        Role role = new Role();
        role.setRoleName("ROLE_ADMIN");
        roleRepository.save(role);

        Optional<Role> result = roleRepository.findByRoleName("ROLE_ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getRoleName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void findByRoleName_shouldReturnEmpty_whenRoleDoesNotExist() {
        Optional<Role> result = roleRepository.findByRoleName("ROLE_NONEXISTENT");

        assertThat(result).isEmpty();
    }

    @Test
    void findByRoleNameIgnoreCase_shouldReturnRole_ignoringCase() {
        Role role = new Role();
        role.setRoleName("ROLE_DONOR");
        roleRepository.save(role);

        Optional<Role> resultLower = roleRepository.findByRoleNameIgnoreCase("role_donor");
        Optional<Role> resultUpper = roleRepository.findByRoleNameIgnoreCase("ROLE_DONOR");

        assertThat(resultLower).isPresent();
        assertThat(resultLower.get().getRoleName()).isEqualTo("ROLE_DONOR");

        assertThat(resultUpper).isPresent();
        assertThat(resultUpper.get().getRoleName()).isEqualTo("ROLE_DONOR");
    }

    @Test
    void findByRoleNameIgnoreCase_shouldReturnEmpty_whenRoleDoesNotExist() {
        Optional<Role> result = roleRepository.findByRoleNameIgnoreCase("role_unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllRoles() {
        Role role1 = new Role();
        role1.setRoleName("ROLE_USER");

        Role role2 = new Role();
        role2.setRoleName("ROLE_ADMIN");

        roleRepository.saveAll(List.of(role1, role2));

        List<Role> roles = roleRepository.findAll();

        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(Role::getRoleName).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void deleteRole_shouldDeleteRoleSuccessfully() {
        Role role = new Role();
        role.setRoleName("ROLE_TEMP");
        Role savedRole = roleRepository.save(role);

        roleRepository.delete(savedRole);

        Optional<Role> result = roleRepository.findById(savedRole.getId());
        assertThat(result).isEmpty();
    }
}
