package com.example.demo.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.example.demo.dto.request.BloodRequestRequest;
import com.example.demo.entity.BloodRequest;
import com.example.demo.entity.BloodStock;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.RequestStatus;
import com.example.demo.repository.BloodRequestRepository;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BloodRequestIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private BloodRequestRepository bloodRequestRepository;

        @Autowired
        private BloodStockRepository bloodStockRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        private User testUser;

        private BloodRequest testRequest;

        // =========================================================
        // SETUP
        // =========================================================

        @BeforeEach
        void setUp() {

                bloodRequestRepository.deleteAll();
                bloodStockRepository.deleteAll();
                userRepository.findByEmail("bloodrequest@gmail.com").ifPresent(userRepository::delete);

                // -----------------------------------------------------
                // USER ROLE
                // -----------------------------------------------------

                Role userRole = roleRepository.findByRoleNameIgnoreCase("USER")
                                .orElseGet(() -> {

                                        Role role = new Role();
                                        role.setRoleName("USER");

                                        return roleRepository.save(role);
                                });

                // -----------------------------------------------------
                // INITIALIZE BLOOD STOCK FOR APPROVAL TESTS
                // -----------------------------------------------------

                BloodStock stockO = new BloodStock();
                stockO.setBloodGroup(BloodGroup.O_POSITIVE);
                stockO.setUnitsAvailable(10);
                stockO.setLastUpdated(LocalDateTime.now());
                bloodStockRepository.save(stockO);

                BloodStock stockA = new BloodStock();
                stockA.setBloodGroup(BloodGroup.A_POSITIVE);
                stockA.setUnitsAvailable(10);
                stockA.setLastUpdated(LocalDateTime.now());
                bloodStockRepository.save(stockA);

                // -----------------------------------------------------
                // CREATE TEST USER
                // -----------------------------------------------------

                testUser = new User();

                testUser.setFullName("Blood Request User");
                testUser.setEmail("bloodrequest@gmail.com");
                testUser.setPassword("Password@123");
                testUser.setPhone("9876543210");
                testUser.setEnabled(true);
                testUser.setCreatedAt(LocalDateTime.now());
                testUser.setRole(userRole);

                testUser = userRepository.save(testUser);

                // -----------------------------------------------------
                // CREATE BLOOD REQUEST
                // -----------------------------------------------------

                testRequest = new BloodRequest();

                testRequest.setUser(testUser);
                testRequest.setBloodGroup(BloodGroup.O_POSITIVE);
                testRequest.setUnitsRequired(2);
                testRequest.setReason("Urgent medical requirement");
                testRequest.setRequestDate(LocalDateTime.now());
                testRequest.setStatus(RequestStatus.PENDING);
                testRequest.setDeleted(false);

                testRequest = bloodRequestRepository.save(testRequest);
        }

        // =========================================================
        // GET MY REQUESTS
        // =========================================================

        @Test
        @WithMockUser(username = "bloodrequest@gmail.com", roles = "USER")
        void getMyRequests_shouldReturn200() throws Exception {

                mockMvc.perform(
                                get("/api/bloodrequests/my"))
                                .andExpect(status().isOk());
        }

        // =========================================================
        // GET ALL REQUESTS
        // =========================================================

        @Test
        @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
        void getAllRequests_shouldReturn200_forAdmin()
                        throws Exception {

                mockMvc.perform(
                                get("/api/bloodrequests"))
                                .andExpect(status().isOk());
        }

        // =========================================================
        // GET REQUEST BY ID
        // =========================================================

        @Test
        @WithMockUser(username = "bloodrequest@gmail.com", roles = "USER")
        void getRequestById_shouldReturn200_whenRequestExists()
                        throws Exception {

                mockMvc.perform(
                                get("/api/bloodrequests/{id}",
                                                testRequest.getId()))
                                .andExpect(status().isOk());
        }

        // =========================================================
        // GET REQUEST BY INVALID ID
        // =========================================================

        @Test
        @WithMockUser(username = "bloodrequest@gmail.com", roles = "USER")
        void getRequestById_shouldReturn404_whenRequestDoesNotExist()
                        throws Exception {

                mockMvc.perform(
                                get("/api/bloodrequests/{id}", 999999L))
                                .andExpect(status().isNotFound());
        }

        // =========================================================
        // CREATE BLOOD REQUEST
        // =========================================================

        @Test
        @WithMockUser(username = "bloodrequest@gmail.com", roles = "USER")
        void createRequest_shouldReturn201_whenRequestIsValid()
                        throws Exception {

                BloodRequestRequest request = new BloodRequestRequest();

                request.setBloodGroup(BloodGroup.A_POSITIVE);
                request.setUnitsRequired(3);
                request.setReason("Surgery requirement");

                mockMvc.perform(
                                post("/api/bloodrequests")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        // =========================================================
        // GET REQUESTS BY STATUS
        // =========================================================

        @Test
        @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
        void getRequestsByStatus_shouldReturn200()
                        throws Exception {

                mockMvc.perform(
                                get("/api/bloodrequests/status/PENDING"))
                                .andExpect(status().isOk());
        }

        // =========================================================
        // APPROVE REQUEST
        // =========================================================

        @Test
        @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
        void approveRequest_shouldReturn200_whenRequestIsValid()
                        throws Exception {

                mockMvc.perform(
                                put("/api/bloodrequests/{id}/approve",
                                                testRequest.getId()))
                                .andExpect(status().isOk());
        }

        // =========================================================
        // REJECT REQUEST
        // =========================================================

        @Test
        @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
        void rejectRequest_shouldReturn200_whenRequestExists()
                        throws Exception {

                mockMvc.perform(
                                put("/api/bloodrequests/{id}/reject",
                                                testRequest.getId()))
                                .andExpect(status().isOk());
        }

        // =========================================================
        // APPROVE REQUEST - INVALID ID
        // =========================================================

        @Test
        @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
        void approveRequest_shouldReturn404_whenRequestDoesNotExist()
                        throws Exception {

                mockMvc.perform(
                                put("/api/bloodrequests/{id}/approve",
                                                999999L))
                                .andExpect(status().isNotFound());
        }

        // =========================================================
        // REJECT REQUEST - INVALID ID
        // =========================================================

        @Test
        @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
        void rejectRequest_shouldReturn404_whenRequestDoesNotExist()
                        throws Exception {

                mockMvc.perform(
                                put("/api/bloodrequests/{id}/reject",
                                                999999L))
                                .andExpect(status().isNotFound());
        }

        // =========================================================
        // USER CANNOT APPROVE REQUEST
        // =========================================================

        @Test
        @WithMockUser(username = "bloodrequest@gmail.com", roles = "USER")
        void approveRequest_shouldReturn403_forUser()
                        throws Exception {

                mockMvc.perform(
                                put("/api/bloodrequests/{id}/approve",
                                                testRequest.getId()))
                                .andExpect(status().isForbidden());
        }

        // =========================================================
        // USER CANNOT REJECT REQUEST
        // =========================================================

        @Test
        @WithMockUser(username = "bloodrequest@gmail.com", roles = "USER")
        void rejectRequest_shouldReturn403_forUser()
                        throws Exception {

                mockMvc.perform(
                                put("/api/bloodrequests/{id}/reject",
                                                testRequest.getId()))
                                .andExpect(status().isForbidden());
        }

        // =========================================================
        // UNAUTHENTICATED USER
        // =========================================================

        @Test
        void getRequests_shouldReturn401_whenNotAuthenticated()
                        throws Exception {

                mockMvc.perform(
                                get("/api/bloodrequests"))
                                .andExpect(status().isUnauthorized());
        }
}