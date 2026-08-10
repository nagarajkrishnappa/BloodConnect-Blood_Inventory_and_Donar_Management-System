package com.example.demo.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User user;
    private User admin;

    // ============================================================
    // SETUP
    // ============================================================

    @BeforeEach
    void setUp() {

        userRepository.findByEmail("testuser@gmail.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("admin@gmail.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("newuser@gmail.com").ifPresent(userRepository::delete);

        /*
         * USER ROLE
         */
        Role userRole = roleRepository.findByRoleNameIgnoreCase("USER")
                .orElseGet(() -> {

                    Role role = new Role();

                    role.setRoleName("USER");

                    return roleRepository.save(role);
                });

        /*
         * ADMIN ROLE
         */
        Role adminRole = roleRepository.findByRoleNameIgnoreCase("ADMIN")
                .orElseGet(() -> {

                    Role role = new Role();

                    role.setRoleName("ADMIN");

                    return roleRepository.save(role);
                });

        /*
         * NORMAL USER
         */
        user = new User();

        user.setFullName("Test User");
        user.setEmail("testuser@gmail.com");
        user.setPassword(
                passwordEncoder.encode("Password@123"));
        user.setPhone("9876543210");
        user.setEnabled(true);
        user.setRole(userRole);

        user = userRepository.save(user);

        /*
         * ADMIN USER
         */
        admin = new User();

        admin.setFullName("Test Admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword(
                passwordEncoder.encode("Admin@123"));
        admin.setPhone("9876543211");
        admin.setEnabled(true);
        admin.setRole(adminRole);

        admin = userRepository.save(admin);
    }

    // ============================================================
    // 1. PUBLIC API
    // ============================================================

    @Test
    void registerEndpoint_shouldBeAccessibleWithoutJwt()
            throws Exception {

        String requestBody = """
                {
                    "fullName": "New User",
                    "email": "newuser@gmail.com",
                    "password": "Password@123",
                    "phone": "9876543212"
                }
                """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ============================================================
    // 2. PROTECTED API WITHOUT TOKEN
    // ============================================================

    @Test
    void protectedApi_shouldReturn401_whenJwtIsMissing()
            throws Exception {

        mockMvc.perform(
                get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // 3. INVALID JWT
    // ============================================================

    @Test
    void protectedApi_shouldReturn401_whenJwtIsInvalid()
            throws Exception {

        mockMvc.perform(
                get("/api/users")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // 4. USER ACCESSING ADMIN API
    // ============================================================

    @Test
    void user_shouldReceive403_whenAccessingAdminEndpoint()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(user));

        mockMvc.perform(
                get("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 5. ADMIN ACCESSING ADMIN API
    // ============================================================

    @Test
    void admin_shouldAccessUserManagementEndpoint()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(admin));

        mockMvc.perform(
                get("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 6. USER ACCESSING GENERAL API
    // ============================================================

    @Test
    void user_shouldAccessAuthenticatedApi()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(user));

        mockMvc.perform(
                get("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 7. ADMIN ACCESSING GENERAL API
    // ============================================================

    @Test
    void admin_shouldAccessAuthenticatedApi()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(admin));

        mockMvc.perform(
                get("/api/donors")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 8. USER CANNOT DELETE DONOR
    // ============================================================

    @Test
    void user_shouldReceive403_whenDeletingDonor()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(user));

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/donors/1")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 9. USER CANNOT UPDATE DONOR
    // ============================================================

    @Test
    void user_shouldReceive403_whenUpdatingDonor()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(user));

        String requestBody = """
                {
                    "bloodGroup": "O_POSITIVE",
                    "gender": "MALE",
                    "address": "Bangalore",
                    "city": "Bangalore",
                    "state": "Karnataka",
                    "pincode": "560001",
                    "weight": 70
                }
                """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/donors/1")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 10. USER CANNOT DELETE BLOOD STOCK
    // ============================================================

    @Test
    void user_shouldReceive403_whenDeletingBloodStock()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(user));

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/bloodstock/1")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 11. USER CANNOT APPROVE BLOOD REQUEST
    // ============================================================

    @Test
    void user_shouldReceive403_whenApprovingBloodRequest()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(user));

        mockMvc.perform(
                post("/api/bloodrequests/1/approve")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 12. USER CANNOT REJECT BLOOD REQUEST
    // ============================================================

    @Test
    void user_shouldReceive403_whenRejectingBloodRequest()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(user));

        mockMvc.perform(
                post("/api/bloodrequests/1/reject")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 13. ADMIN CAN DELETE BLOOD STOCK
    // ============================================================

    @Test
    void admin_shouldNotReceive403_whenDeletingBloodStock()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(admin));

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/bloodstock/999999")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                /*
                 * Security should allow the request.
                 *
                 * The actual response may be 200/404/500
                 * depending on service implementation.
                 *
                 * We are specifically checking that it
                 * does NOT fail with 401/403.
                 */
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        401,
                        result.getResponse().getStatus()))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        403,
                        result.getResponse().getStatus()));
    }

    // ============================================================
    // 14. ADMIN CAN UPDATE BLOOD STOCK
    // ============================================================

    @Test
    void admin_shouldNotReceive403_whenUpdatingBloodStock()
            throws Exception {

        String token = jwtService.generateToken(
                new UserPrincipal(admin));

        String requestBody = """
                {
                    "bloodGroup": "O_POSITIVE",
                    "units": 10
                }
                """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/bloodstock/999999")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        401,
                        result.getResponse().getStatus()))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        403,
                        result.getResponse().getStatus()));
    }
}