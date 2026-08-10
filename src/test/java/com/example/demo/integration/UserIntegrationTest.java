package com.example.demo.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.demo.repository.RoleRepository roleRepository;

    private String userToken;
    private Long userId;

    // ============================================================
    // CREATE TEST USER AND GET JWT TOKEN
    // ============================================================

    @BeforeEach
    void setUp() throws Exception {

        // Remove previous test users if they exist
        userRepository.findByEmail("userintegration@gmail.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("updatedintegration@gmail.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("testupdate@gmail.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("unauthorized@gmail.com").ifPresent(userRepository::delete);

        // Register test user through actual API
        RegisterRequest registerRequest = new RegisterRequest();

        registerRequest.setFullName("Integration User");
        registerRequest.setEmail("userintegration@gmail.com");
        registerRequest.setPassword("Password@123");
        registerRequest.setPhone("9999999999");

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        registerRequest)))
                .andExpect(status().isCreated());

        // Fetch created user from database and elevate to ADMIN role for user-management tests
        User user = userRepository
                .findByEmail("userintegration@gmail.com")
                .orElseThrow();

        com.example.demo.entity.Role adminRole = roleRepository
                .findByRoleNameIgnoreCase("ADMIN")
                .orElseGet(() -> roleRepository.save(new com.example.demo.entity.Role(null, "ADMIN")));

        user.setRole(adminRole);
        userRepository.save(user);

        userId = user.getId();

        assertNotNull(userId);

        // Login through actual API
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setEmail("userintegration@gmail.com");
        loginRequest.setPassword("Password@123");

        String loginResponse = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);

        userToken = jsonNode.get("token").asText();

        assertNotNull(userToken);
        assertTrue(!userToken.isBlank());
    }

    // ============================================================
    // GET ALL USERS - AUTHENTICATED USER
    // ============================================================

    @Test
    void getAllUsers_shouldReturn200_whenAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // GET USER BY ID - SUCCESS
    // ============================================================

    @Test
    void getUserById_shouldReturn200_whenUserExists()
            throws Exception {

        mockMvc.perform(
                get("/api/users/" + userId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // GET USER BY ID - NOT FOUND
    // ============================================================

    @Test
    void getUserById_shouldReturn404_whenUserDoesNotExist()
            throws Exception {

        mockMvc.perform(
                get("/api/users/999999999")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // UPDATE USER - SUCCESS
    // ============================================================

    @Test
    void updateUser_shouldReturn200_whenRequestIsValid()
            throws Exception {

        com.example.demo.dto.request.UserUpdateRequest request = new com.example.demo.dto.request.UserUpdateRequest();

        request.setFullName("Updated Integration User");
        request.setEmail("updatedintegration@gmail.com");
        request.setPhone("8888888888");

        mockMvc.perform(
                put("/api/users/" + userId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify database was actually updated
        User updatedUser = userRepository
                .findById(userId)
                .orElseThrow();

        assertTrue(
                updatedUser.getFullName()
                        .equals("Updated Integration User"));

        assertTrue(
                updatedUser.getEmail()
                        .equals("updatedintegration@gmail.com"));
    }

    // ============================================================
    // UPDATE USER - INVALID REQUEST
    // ============================================================

    @Test
    void updateUser_shouldReturn400_whenRequestIsInvalid()
            throws Exception {

        com.example.demo.dto.request.UserUpdateRequest request = new com.example.demo.dto.request.UserUpdateRequest();

        request.setFullName("");
        request.setEmail("");
        request.setPhone("123");

        mockMvc.perform(
                put("/api/users/" + userId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // UPDATE USER - NOT FOUND
    // ============================================================

    @Test
    void updateUser_shouldReturn404_whenUserDoesNotExist()
            throws Exception {

        com.example.demo.dto.request.UserUpdateRequest request = new com.example.demo.dto.request.UserUpdateRequest();

        request.setFullName("Test User");
        request.setEmail("testupdate@gmail.com");
        request.setPhone("7777777777");

        mockMvc.perform(
                put("/api/users/999999999")
                        .header(
                                "Authorization",
                                "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // DELETE USER - SUCCESS
    // ============================================================

    @Test
    void deleteUser_shouldReturn200_whenUserExists()
            throws Exception {

        mockMvc.perform(
                delete("/api/users/" + userId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk());

        // Verify user was deleted
        assertTrue(
                userRepository.findById(userId).isEmpty());
    }

    // ============================================================
    // DELETE USER - NOT FOUND
    // ============================================================

    @Test
    void deleteUser_shouldReturn404_whenUserDoesNotExist()
            throws Exception {

        mockMvc.perform(
                delete("/api/users/999999999")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // GET USERS - WITHOUT TOKEN
    // ============================================================

    @Test
    void getAllUsers_shouldReturn401_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // GET USER - WITHOUT TOKEN
    // ============================================================

    @Test
    void getUserById_shouldReturn401_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/users/" + userId))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // UPDATE USER - WITHOUT TOKEN
    // ============================================================

    @Test
    void updateUser_shouldReturn401_whenNotAuthenticated()
            throws Exception {

        com.example.demo.dto.request.UserUpdateRequest request = new com.example.demo.dto.request.UserUpdateRequest();

        request.setFullName("Unauthorized Update");
        request.setEmail("unauthorized@gmail.com");
        request.setPhone("6666666666");

        mockMvc.perform(
                put("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // DELETE USER - WITHOUT TOKEN
    // ============================================================

    @Test
    void deleteUser_shouldReturn401_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                delete("/api/users/" + userId))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // WRONG HTTP METHOD
    // ============================================================

    @Test
    void getUsers_shouldReturn405_whenUsingPost()
            throws Exception {

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isMethodNotAllowed());
    }
}