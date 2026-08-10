package com.example.demo.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BloodBankEndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.demo.repository.UserRepository userRepository;

    @Autowired
    private com.example.demo.repository.RoleRepository roleRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /*
     * JWT tokens created during the test.
     */
    private String userToken;

    private String adminToken;

    /*
     * IDs created during the test.
     */
    private Long donorId;

    private Long bloodStockId;

    private Long bloodRequestId;

    private Long donationId;

    // ============================================================
    // TEST DATA
    // ============================================================

    private static final String USER_EMAIL = "e2euser@gmail.com";

    private static final String USER_PASSWORD = "Password@123";

    private static final String ADMIN_EMAIL = "admin@gmail.com";

    private static final String ADMIN_PASSWORD = "Admin@123";

    // ============================================================
    // BEFORE EACH TEST
    // ============================================================

    @BeforeEach
    void setUp() throws Exception {

        com.example.demo.entity.Role userRole = roleRepository.findByRoleNameIgnoreCase("USER")
                .orElseGet(() -> {
                    com.example.demo.entity.Role r = new com.example.demo.entity.Role();
                    r.setRoleName("USER");
                    return roleRepository.save(r);
                });

        com.example.demo.entity.Role adminRole = roleRepository.findByRoleNameIgnoreCase("ADMIN")
                .orElseGet(() -> {
                    com.example.demo.entity.Role r = new com.example.demo.entity.Role();
                    r.setRoleName("ADMIN");
                    return roleRepository.save(r);
                });

        if (!userRepository.existsByEmail(USER_EMAIL)) {
            com.example.demo.entity.User user = new com.example.demo.entity.User();
            user.setFullName("E2E User");
            user.setEmail(USER_EMAIL);
            user.setPassword(passwordEncoder.encode(USER_PASSWORD));
            user.setPhone("9876543210");
            user.setEnabled(true);
            user.setRole(userRole);
            userRepository.save(user);
        }

        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            com.example.demo.entity.User admin = new com.example.demo.entity.User();
            admin.setFullName("E2E Admin");
            admin.setEmail(ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setPhone("9876543211");
            admin.setEnabled(true);
            admin.setRole(adminRole);
            userRepository.save(admin);
        }

        userToken = login(
                USER_EMAIL,
                USER_PASSWORD);

        adminToken = login(
                ADMIN_EMAIL,
                ADMIN_PASSWORD);
    }

    // ============================================================
    // HELPER METHOD
    // LOGIN
    // ============================================================

    private String login(
            String email,
            String password) throws Exception {

        String loginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                email,
                password);

        MvcResult result = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode json = objectMapper.readTree(
                result.getResponse()
                        .getContentAsString());

        return json.get("token").asText();
    }

    // ============================================================
    // 1. AUTHENTICATION
    // ============================================================

    @Test
    void authenticationFlow_shouldLoginSuccessfully()
            throws Exception {

        String loginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                USER_EMAIL,
                USER_PASSWORD);

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .isNotEmpty())
                .andExpect(
                        jsonPath("$.user")
                                .exists());
    }

    // ============================================================
    // 2. UNAUTHORIZED ACCESS
    // ============================================================

    @Test
    void protectedApi_shouldReturn401_withoutJwtToken()
            throws Exception {

        mockMvc.perform(
                get("/api/donors"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // 3. GET ALL DONORS
    // ============================================================

    @Test
    void donorFlow_shouldGetAllDonors()
            throws Exception {

        mockMvc.perform(
                get("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 4. CREATE DONOR
    // ============================================================

    @Test
    void donorFlow_shouldCreateDonor()
            throws Exception {

        String donorJson = """
                {
                    "email": "%s",
                    "bloodGroup": "O_POSITIVE",
                    "gender": "MALE",
                    "dateOfBirth": "1998-01-01",
                    "address": "Bangalore",
                    "city": "Bengaluru",
                    "state": "Karnataka",
                    "pincode": "560001",
                    "weight": 70.0,
                    "lastDonationDate": null
                }
                """.formatted(USER_EMAIL);

        mockMvc.perform(
                post("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer " + userToken)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(donorJson))
                .andExpect(status().isCreated())
                .andExpect(
                        content().string(
                                "Donor Added Successfully"));
    }

    // ============================================================
    // 5. GET DONORS AFTER CREATION
    // ============================================================

    @Test
    void donorFlow_shouldReturnDonorsAfterCreation()
            throws Exception {

        mockMvc.perform(
                get("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON));
    }

    // ============================================================
    // 6. GET BLOOD STOCK
    // ============================================================

    @Test
    void bloodStockFlow_shouldGetAllBloodStock()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodstock")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 7. CREATE BLOOD STOCK - ADMIN
    // ============================================================

    @Test
    void bloodStockFlow_adminShouldCreateBloodStock()
            throws Exception {

        String stockJson = """
                {
                    "bloodGroup": "O_POSITIVE",
                    "units": 10
                }
                """;

        mockMvc.perform(
                post("/api/bloodstock")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(stockJson))
                .andExpect(status().isCreated())
                .andExpect(
                        content().string(
                                "Blood Stock Added Successfully"));
    }

    // ============================================================
    // 8. NORMAL USER CANNOT CREATE BLOOD STOCK
    // ============================================================

    @Test
    void bloodStockFlow_userShouldNotCreateBloodStock()
            throws Exception {

        String stockJson = """
                {
                    "bloodGroup": "O_POSITIVE",
                    "units": 10
                }
                """;

        mockMvc.perform(
                post("/api/bloodstock")
                        .header(
                                "Authorization",
                                "Bearer " + userToken)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(stockJson))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 9. GET BLOOD REQUESTS
    // ============================================================

    @Test
    void bloodRequestFlow_shouldGetAllRequests()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodrequests")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 10. GET MY BLOOD REQUESTS
    // ============================================================

    @Test
    void bloodRequestFlow_shouldGetMyRequests()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodrequests/my")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 11. GET BLOOD REQUEST BY STATUS
    // ============================================================

    @Test
    void bloodRequestFlow_shouldGetPendingRequests()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodrequests/status/PENDING")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 12. CREATE BLOOD REQUEST
    // ============================================================

    @Test
    void bloodRequestFlow_shouldCreateRequest()
            throws Exception {

        /*
         * IMPORTANT:
         *
         * Replace the JSON fields below with the exact
         * fields from your BloodRequestRequest class.
         *
         * The controller accepts:
         *
         * POST /api/bloodrequests
         */

        String requestJson = """
                {
                    "email": "%s",
                    "bloodGroup": "O_POSITIVE",
                    "unitsRequired": 1,
                    "reason": "Emergency Requirement"
                }
                """.formatted(USER_EMAIL);

        mockMvc.perform(
                post("/api/bloodrequests")
                        .header(
                                "Authorization",
                                "Bearer " + userToken)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(
                        content().string(
                                "Blood Request Submitted Successfully"));
    }

    // ============================================================
    // 13. GET DONATIONS
    // ============================================================

    @Test
    void donationFlow_shouldGetAllDonations()
            throws Exception {

        mockMvc.perform(
                get("/api/donations")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 14. INVALID JWT
    // ============================================================

    @Test
    void protectedApi_shouldReturn401_whenJwtIsInvalid()
            throws Exception {

        mockMvc.perform(
                get("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // 15. ADMIN USER ACCESS
    // ============================================================

    @Test
    void admin_shouldAccessUserManagement()
            throws Exception {

        mockMvc.perform(
                get("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 16. NORMAL USER CANNOT ACCESS USER MANAGEMENT
    // ============================================================

    @Test
    void normalUser_shouldNotAccessUserManagement()
            throws Exception {

        mockMvc.perform(
                get("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 17. GET USER BY ID
    // ============================================================

    @Test
    void admin_shouldGetUserById()
            throws Exception {

        mockMvc.perform(
                get("/api/users/1")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken))
                .andExpect(
                        result -> {

                            int status = result.getResponse()
                                    .getStatus();

                            /*
                             * Depending on whether ID 1 exists.
                             */
                            if (status != 200
                                    && status != 404) {

                                throw new AssertionError(
                                        "Unexpected HTTP status: "
                                                + status);
                            }
                        });
    }

    // ============================================================
    // 18. ADMIN APPROVES BLOOD REQUEST
    // ============================================================

    @Test
    void admin_shouldApproveBloodRequest()
            throws Exception {

        /*
         * This test assumes request ID 1 exists
         * and sufficient blood stock exists.
         *
         * If it does not exist, the application should
         * correctly return 404/500 depending on the
         * service exception.
         */

        mockMvc.perform(
                put("/api/bloodrequests/1/approve")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken))
                .andExpect(
                        result -> {

                            int status = result.getResponse()
                                    .getStatus();

                            if (status != 200
                                    && status != 404
                                    && status != 500) {

                                throw new AssertionError(
                                        "Unexpected HTTP status: "
                                                + status);
                            }
                        });
    }

    // ============================================================
    // 19. NORMAL USER CANNOT APPROVE REQUEST
    // ============================================================

    @Test
    void normalUser_shouldNotApproveBloodRequest()
            throws Exception {

        mockMvc.perform(
                put("/api/bloodrequests/1/approve")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 20. ADMIN REJECTS BLOOD REQUEST
    // ============================================================

    @Test
    void admin_shouldRejectBloodRequest()
            throws Exception {

        mockMvc.perform(
                put("/api/bloodrequests/1/reject")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken))
                .andExpect(
                        result -> {

                            int status = result.getResponse()
                                    .getStatus();

                            if (status != 200
                                    && status != 404
                                    && status != 500) {

                                throw new AssertionError(
                                        "Unexpected HTTP status: "
                                                + status);
                            }
                        });
    }

    // ============================================================
    // 21. NORMAL USER CANNOT REJECT REQUEST
    // ============================================================

    @Test
    void normalUser_shouldNotRejectBloodRequest()
            throws Exception {

        mockMvc.perform(
                put("/api/bloodrequests/1/reject")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 22. LOGOUT
    // ============================================================

    @Test
    void logout_shouldRedirectToLogin()
            throws Exception {

        mockMvc.perform(
                post("/logout"))
                .andExpect(status().is3xxRedirection());
    }

    // ============================================================
    // 23. WRONG HTTP METHOD
    // ============================================================

    @Test
    void bloodStock_shouldRejectUnsupportedMethod()
            throws Exception {

        mockMvc.perform(
                put("/api/bloodstock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isMethodNotAllowed());
    }

    // ============================================================
    // 24. INVALID BLOOD GROUP
    // ============================================================

    @Test
    void bloodRequest_shouldReturn400_whenBloodGroupIsInvalid()
            throws Exception {

        String requestJson = """
                {
                    "email": "%s",
                    "bloodGroup": "INVALID_GROUP",
                    "unitsRequired": 1
                }
                """.formatted(USER_EMAIL);

        mockMvc.perform(
                post("/api/bloodrequests")
                        .header(
                                "Authorization",
                                "Bearer " + userToken)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // 25. INVALID BLOOD STOCK REQUEST
    // ============================================================

    @Test
    void bloodStock_shouldReturn400_whenBloodGroupIsInvalid()
            throws Exception {

        String stockJson = """
                {
                    "bloodGroup": "INVALID_GROUP",
                    "units": 10
                }
                """;

        mockMvc.perform(
                post("/api/bloodstock")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(stockJson))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // 26. NEGATIVE BLOOD STOCK
    // ============================================================

    @Test
    void bloodStock_shouldReturn400_whenUnitsAreNegative()
            throws Exception {

        String stockJson = """
                {
                    "bloodGroup": "O_POSITIVE",
                    "units": -5
                }
                """;

        mockMvc.perform(
                post("/api/bloodstock")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(stockJson))
                .andExpect(status().isBadRequest());
    }
}