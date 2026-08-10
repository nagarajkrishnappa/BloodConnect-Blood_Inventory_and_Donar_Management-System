package com.example.demo.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.ActiveProfiles;

import com.example.demo.dto.request.DonorRequest;
import com.example.demo.entity.Donor;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.Gender;
import com.example.demo.repository.DonorRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DonorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private com.example.demo.repository.DonationHistoryRepository donationHistoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User adminUser;
    private User normalUser;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {

        /*
         * Clean donation history and donor data created by previous test.
         */
        donationHistoryRepository.deleteAll();
        donorRepository.deleteAll();

        /*
         * Remove test users if they already exist.
         */
        userRepository.findByEmail("donor.admin@test.com")
                .ifPresent(userRepository::delete);

        userRepository.findByEmail("donor.user@test.com")
                .ifPresent(userRepository::delete);

        /*
         * Find existing ADMIN role.
         */
        Role adminRole = roleRepository
                .findByRoleNameIgnoreCase("ADMIN")
                .orElseGet(() -> {

                    Role role = new Role();
                    role.setRoleName("ADMIN");

                    return roleRepository.save(role);
                });

        /*
         * Find existing USER role.
         */
        Role userRole = roleRepository
                .findByRoleNameIgnoreCase("USER")
                .orElseGet(() -> {

                    Role role = new Role();
                    role.setRoleName("USER");

                    return roleRepository.save(role);
                });

        /*
         * Create ADMIN user.
         */
        adminUser = new User();

        adminUser.setFullName("Integration Admin");
        adminUser.setEmail("donor.admin@test.com");
        adminUser.setPassword(
                passwordEncoder.encode("Password@123"));
        adminUser.setPhone("9876543210");
        adminUser.setEnabled(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setRole(adminRole);

        adminUser = userRepository.save(adminUser);

        /*
         * Create normal USER.
         */
        normalUser = new User();

        normalUser.setFullName("Integration User");
        normalUser.setEmail("donor.user@test.com");
        normalUser.setPassword(
                passwordEncoder.encode("Password@123"));
        normalUser.setPhone("9876543211");
        normalUser.setEnabled(true);
        normalUser.setCreatedAt(LocalDateTime.now());
        normalUser.setRole(userRole);

        normalUser = userRepository.save(normalUser);

        /*
         * Generate real JWT tokens.
         */
        adminToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        adminUser.getEmail(),
                        adminUser.getPassword(),
                        java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "ROLE_ADMIN"))));

        userToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        normalUser.getEmail(),
                        normalUser.getPassword(),
                        java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "ROLE_USER"))));
    }

    // ============================================================
    // Helper method
    // ============================================================

    private DonorRequest createDonorRequest() {

        DonorRequest request = new DonorRequest();

        request.setEmail("donor.test@example.com");
        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setGender(Gender.MALE);
        request.setDateOfBirth(
                java.time.LocalDate.of(1998, 5, 10));
        request.setAddress("MG Road");
        request.setCity("Bangalore");
        request.setState("Karnataka");
        request.setPincode("560001");
        request.setWeight(70.0);
        request.setLastDonationDate(null);

        return request;
    }

    // ============================================================
    // POST /api/donors
    // ============================================================

    @Test
    void createDonor_shouldReturn201_whenRequestIsValid()
            throws Exception {

        DonorRequest request = createDonorRequest();

        mockMvc.perform(
                post("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // ============================================================
    // GET /api/donors
    // ============================================================

    @Test
    void getAllDonors_shouldReturn200_whenAuthenticated()
            throws Exception {

        createDonorDirectly();

        mockMvc.perform(
                get("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ============================================================
    // GET /api/donors/{id}
    // ============================================================

    @Test
    void getDonorById_shouldReturn200_whenDonorExists()
            throws Exception {

        Donor donor = createDonorDirectly();

        mockMvc.perform(
                get("/api/donors/" + donor.getId())
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(donor.getId()));
    }

    // ============================================================
    // GET /api/donors/{id} - NOT FOUND
    // ============================================================

    @Test
    void getDonorById_shouldReturn404_whenDonorDoesNotExist()
            throws Exception {

        mockMvc.perform(
                get("/api/donors/999999")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // PUT /api/donors/{id}
    // ============================================================

    @Test
    void updateDonor_shouldReturn200_whenDonorExists()
            throws Exception {

        Donor donor = createDonorDirectly();

        DonorRequest request = createDonorRequest();

        request.setCity("Mysore");
        request.setAddress("Mysore Palace Road");
        request.setWeight(72.0);

        mockMvc.perform(
                put("/api/donors/" + donor.getId())
                        .header(
                                "Authorization",
                                "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ============================================================
    // PUT /api/donors/{id} - NOT FOUND
    // ============================================================

    @Test
    void updateDonor_shouldReturn404_whenDonorDoesNotExist()
            throws Exception {

        DonorRequest request = createDonorRequest();

        mockMvc.perform(
                put("/api/donors/999999")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // DELETE /api/donors/{id}
    // ============================================================

    @Test
    void deleteDonor_shouldReturn200_whenDonorExists()
            throws Exception {

        Donor donor = createDonorDirectly();

        mockMvc.perform(
                delete("/api/donors/" + donor.getId())
                        .header(
                                "Authorization",
                                "Bearer " + adminToken))
                .andExpect(status().isOk());

        Donor deletedDonor = donorRepository.findById(donor.getId())
                .orElseThrow();

        /*
         * Your application uses soft delete.
         */
        org.junit.jupiter.api.Assertions.assertTrue(
                deletedDonor.getDeleted());

        org.junit.jupiter.api.Assertions.assertFalse(
                deletedDonor.getAvailable());
    }

    // ============================================================
    // DELETE /api/donors/{id} - NOT FOUND
    // ============================================================

    @Test
    void deleteDonor_shouldReturn404_whenDonorDoesNotExist()
            throws Exception {

        mockMvc.perform(
                delete("/api/donors/999999")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // NO JWT
    // ============================================================

    @Test
    void getAllDonors_shouldReturn401_whenTokenIsMissing()
            throws Exception {

        mockMvc.perform(
                get("/api/donors"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // INVALID JWT
    // ============================================================

    @Test
    void getAllDonors_shouldReturn401_whenTokenIsInvalid()
            throws Exception {

        mockMvc.perform(
                get("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // WRONG HTTP METHOD
    // ============================================================

    @Test
    void getDonorEndpoint_shouldReturn405_whenUsingPostWithoutBody()
            throws Exception {

        mockMvc.perform(
                post("/api/donors/1")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isMethodNotAllowed());
    }

    // ============================================================
    // USER CAN READ DONORS
    // ============================================================

    @Test
    void getDonors_shouldReturn200_forNormalUser()
            throws Exception {

        mockMvc.perform(
                get("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // Helper - create donor directly in database
    // ============================================================

    private Donor createDonorDirectly() {

        Donor donor = new Donor();

        donor.setUser(normalUser);
        donor.setBloodGroup(BloodGroup.O_POSITIVE);
        donor.setGender(Gender.MALE);
        donor.setDateOfBirth(
                java.time.LocalDate.of(1998, 5, 10));
        donor.setAddress("MG Road");
        donor.setCity("Bangalore");
        donor.setState("Karnataka");
        donor.setPincode("560001");
        donor.setWeight(70.0);
        donor.setAvailable(true);
        donor.setDeleted(false);

        return donorRepository.save(donor);
    }
}