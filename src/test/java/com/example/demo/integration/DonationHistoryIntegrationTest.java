package com.example.demo.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.request.DonationHistoryRequest;
import com.example.demo.entity.DonationHistory;
import com.example.demo.entity.Donor;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.Gender;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.repository.DonationHistoryRepository;
import com.example.demo.repository.DonorRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DonationHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DonationHistoryRepository donationHistoryRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BloodStockRepository bloodStockRepository;

    private User testUser;
    private Donor testDonor;
    private DonationHistory testDonation;

    @BeforeEach
    void setUp() {

        donationHistoryRepository.deleteAll();
        donorRepository.deleteAll();
        bloodStockRepository.deleteAll();

        userRepository.findByEmail("donation.user@test.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("donation.admin@test.com").ifPresent(userRepository::delete);

        Role userRole = roleRepository.findByRoleNameIgnoreCase("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("USER");
                    return roleRepository.save(role);
                });

        Role adminRole = roleRepository.findByRoleNameIgnoreCase("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("ADMIN");
                    return roleRepository.save(role);
                });

        testUser = new User();
        testUser.setFullName("Donation Test User");
        testUser.setEmail("donation.user@test.com");
        testUser.setPassword("Password@123");
        testUser.setPhone("9876543210");
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setRole(userRole);
        testUser = userRepository.save(testUser);

        User adminUser = new User();
        adminUser.setFullName("Donation Test Admin");
        adminUser.setEmail("donation.admin@test.com");
        adminUser.setPassword("Password@123");
        adminUser.setPhone("9876543211");
        adminUser.setEnabled(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);

        testDonor = new Donor();
        testDonor.setUser(testUser);
        testDonor.setBloodGroup(BloodGroup.O_POSITIVE);
        testDonor.setGender(Gender.MALE);
        testDonor.setDateOfBirth(LocalDate.of(1995, 5, 15));
        testDonor.setWeight(70.0);
        testDonor.setCity("Test City");
        testDonor.setState("Test State");
        testDonor.setPincode("123456");
        testDonor.setAddress("Test Address");
        testDonor.setAvailable(true);
        testDonor.setDeleted(false);
        testDonor = donorRepository.save(testDonor);

        testDonation = new DonationHistory();
        testDonation.setDonor(testDonor);
        testDonation.setDonationDate(LocalDate.now());
        testDonation.setBloodGroup(BloodGroup.O_POSITIVE.getValue());
        testDonation.setUnitsDonated(1);
        testDonation.setRemarks("Routine donation");
        testDonation.setDeleted(false);
        testDonation = donationHistoryRepository.save(testDonation);
    }

    // =========================================================
    // GET ALL DONATIONS
    // =========================================================

    @Test
    @WithMockUser(username = "donation.user@test.com", roles = "USER")
    void getAllDonations_shouldReturn200_forAuthenticatedUser() throws Exception {

        mockMvc.perform(get("/api/donations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // =========================================================
    // GET DONATION BY ID
    // =========================================================

    @Test
    @WithMockUser(username = "donation.user@test.com", roles = "USER")
    void getDonationById_shouldReturn200_whenDonationExists() throws Exception {

        mockMvc.perform(get("/api/donations/{id}", testDonation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDonation.getId()));
    }

    @Test
    @WithMockUser(username = "donation.user@test.com", roles = "USER")
    void getDonationById_shouldReturn404_whenDonationDoesNotExist() throws Exception {

        mockMvc.perform(get("/api/donations/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // ADD DONATION
    // =========================================================

    @Test
    @WithMockUser(username = "donation.user@test.com", roles = "USER")
    void addDonation_shouldReturn201_whenRequestIsValid() throws Exception {

        DonationHistoryRequest request = new DonationHistoryRequest();
        request.setDonorId(testDonor.getId());
        request.setDonationDate(LocalDate.now());
        request.setUnitsDonated(1);
        request.setRemarks("New blood donation");

        mockMvc.perform(post("/api/donations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "donation.user@test.com", roles = "USER")
    void addDonation_shouldReturn404_whenDonorDoesNotExist() throws Exception {

        DonationHistoryRequest request = new DonationHistoryRequest();
        request.setDonorId(999999L);
        request.setDonationDate(LocalDate.now());
        request.setUnitsDonated(1);
        request.setRemarks("Invalid donor donation");

        mockMvc.perform(post("/api/donations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // UPDATE DONATION
    // =========================================================

    @Test
    @WithMockUser(username = "donation.admin@test.com", roles = "ADMIN")
    void updateDonation_shouldReturn200_forAdmin() throws Exception {

        DonationHistoryRequest request = new DonationHistoryRequest();
        request.setDonorId(testDonor.getId());
        request.setDonationDate(LocalDate.now());
        request.setUnitsDonated(2);
        request.setRemarks("Updated remarks");

        mockMvc.perform(put("/api/donations/{id}", testDonation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "donation.user@test.com", roles = "USER")
    void updateDonation_shouldReturn403_forUser() throws Exception {

        DonationHistoryRequest request = new DonationHistoryRequest();
        request.setDonorId(testDonor.getId());
        request.setDonationDate(LocalDate.now());
        request.setUnitsDonated(2);

        mockMvc.perform(put("/api/donations/{id}", testDonation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "donation.admin@test.com", roles = "ADMIN")
    void updateDonation_shouldReturn404_whenDonationDoesNotExist() throws Exception {

        DonationHistoryRequest request = new DonationHistoryRequest();
        request.setDonorId(testDonor.getId());
        request.setDonationDate(LocalDate.now());
        request.setUnitsDonated(2);

        mockMvc.perform(put("/api/donations/{id}", 999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // DELETE DONATION
    // =========================================================

    @Test
    @WithMockUser(username = "donation.admin@test.com", roles = "ADMIN")
    void deleteDonation_shouldReturn200_forAdmin() throws Exception {

        mockMvc.perform(delete("/api/donations/{id}", testDonation.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "donation.user@test.com", roles = "USER")
    void deleteDonation_shouldReturn403_forUser() throws Exception {

        mockMvc.perform(delete("/api/donations/{id}", testDonation.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "donation.admin@test.com", roles = "ADMIN")
    void deleteDonation_shouldReturn404_whenDonationDoesNotExist() throws Exception {

        mockMvc.perform(delete("/api/donations/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // UNAUTHENTICATED
    // =========================================================

    @Test
    void getDonations_shouldReturn401_whenNotAuthenticated() throws Exception {

        mockMvc.perform(get("/api/donations"))
                .andExpect(status().isUnauthorized());
    }
}
