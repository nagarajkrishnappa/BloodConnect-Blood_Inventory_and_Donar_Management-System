package com.example.demo.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @BeforeEach
        void setUp() {
                userRepository.findByEmail("integrationtest@gmail.com").ifPresent(userRepository::delete);
                userRepository.findByEmail("duplicate@gmail.com").ifPresent(userRepository::delete);
                userRepository.findByEmail("loginuser@gmail.com").ifPresent(userRepository::delete);
                userRepository.findByEmail("wrongpassword@gmail.com").ifPresent(userRepository::delete);
                userRepository.findByEmail("publicregister@gmail.com").ifPresent(userRepository::delete);
                userRepository.findByEmail("publiclogin@gmail.com").ifPresent(userRepository::delete);
        }

        // ============================================================
        // REGISTER - SUCCESS
        // ============================================================

        @Test
        void registerUser_shouldCreateUserSuccessfully() throws Exception {

                RegisterRequest request = new RegisterRequest();

                request.setFullName("Integration Test User");
                request.setEmail("integrationtest@gmail.com");
                request.setPassword("Password@123");
                request.setPhone("9999999999");

                mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());

                // Verify that user was actually saved in database
                assertTrue(
                                userRepository.existsByEmail("integrationtest@gmail.com"));
        }

        // ============================================================
        // REGISTER - DUPLICATE EMAIL
        // ============================================================

        @Test
        void registerUser_shouldReturn409_whenEmailAlreadyExists()
                        throws Exception {

                RegisterRequest request = new RegisterRequest();

                request.setFullName("Duplicate User");
                request.setEmail("duplicate@gmail.com");
                request.setPassword("Password@123");
                request.setPhone("8888888888");

                // First registration
                mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());

                // Second registration with same email
                mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        // ============================================================
        // REGISTER - INVALID REQUEST
        // ============================================================

        @Test
        void registerUser_shouldReturn400_whenRequestIsInvalid()
                        throws Exception {

                RegisterRequest request = new RegisterRequest();

                request.setFullName("");
                request.setEmail("");
                request.setPassword("");
                request.setPhone("");

                mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // ============================================================
        // REGISTER - INVALID EMAIL
        // ============================================================

        @Test
        void registerUser_shouldReturn400_whenEmailIsInvalid()
                        throws Exception {

                RegisterRequest request = new RegisterRequest();

                request.setFullName("Test User");
                request.setEmail("invalid-email");
                request.setPassword("Password@123");
                request.setPhone("9999999999");

                mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // ============================================================
        // LOGIN - SUCCESS
        // ============================================================

        @Test
        void loginUser_shouldReturn200_whenCredentialsAreValid()
                        throws Exception {

                // First create the user through the real registration API
                RegisterRequest registerRequest = new RegisterRequest();

                registerRequest.setFullName("Login Test User");
                registerRequest.setEmail("loginuser@gmail.com");
                registerRequest.setPassword("Password@123");
                registerRequest.setPhone("7777777777");

                mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                registerRequest)))
                                .andExpect(status().isCreated());

                // Login
                LoginRequest loginRequest = new LoginRequest();

                loginRequest.setEmail("loginuser@gmail.com");
                loginRequest.setPassword("Password@123");

                String response = mockMvc.perform(
                                post("/api/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                // Verify response contains JWT token
                JsonNode jsonResponse = objectMapper.readTree(response);

                assertTrue(
                                jsonResponse.has("token"),
                                "Login response should contain JWT token");
        }

        // ============================================================
        // LOGIN - WRONG PASSWORD
        // ============================================================

        @Test
        void loginUser_shouldRejectWrongPassword()
                        throws Exception {

                // Create user
                RegisterRequest registerRequest = new RegisterRequest();

                registerRequest.setFullName("Wrong Password User");
                registerRequest.setEmail("wrongpassword@gmail.com");
                registerRequest.setPassword("Password@123");
                registerRequest.setPhone("6666666666");

                mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                registerRequest)))
                                .andExpect(status().isCreated());

                // Login with wrong password
                LoginRequest loginRequest = new LoginRequest();

                loginRequest.setEmail("wrongpassword@gmail.com");
                loginRequest.setPassword("WrongPassword@123");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                loginRequest)))
                                .andExpect(status().isBadRequest());
        }

        // ============================================================
        // LOGIN - USER DOES NOT EXIST
        // ============================================================

        @Test
        void loginUser_shouldRejectUnknownUser()
                        throws Exception {

                LoginRequest loginRequest = new LoginRequest();

                loginRequest.setEmail("doesnotexist@gmail.com");
                loginRequest.setPassword("Password@123");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                loginRequest)))
                                .andExpect(status().isBadRequest());
        }

        // ============================================================
        // LOGIN - INVALID REQUEST
        // ============================================================

        @Test
        void loginUser_shouldReturn400_whenRequestIsInvalid()
                        throws Exception {

                LoginRequest loginRequest = new LoginRequest();

                loginRequest.setEmail("");
                loginRequest.setPassword("");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                loginRequest)))
                                .andExpect(status().isBadRequest());
        }

        // ============================================================
        // REGISTER - WRONG HTTP METHOD
        // ============================================================

        @Test
        void registerUser_shouldReturn405_whenUsingGet()
                        throws Exception {

                mockMvc.perform(
                                get("/api/auth/register"))
                                .andExpect(status().isMethodNotAllowed());
        }

        // ============================================================
        // LOGIN - WRONG HTTP METHOD
        // ============================================================

        @Test
        void loginUser_shouldReturn405_whenUsingGet()
                        throws Exception {

                mockMvc.perform(
                                get("/api/auth/login"))
                                .andExpect(status().isMethodNotAllowed());
        }

        // ============================================================
        // REGISTER - PUBLIC API
        // ============================================================

        @Test
        void registerUser_shouldBeAccessibleWithoutAuthentication()
                        throws Exception {

                RegisterRequest request = new RegisterRequest();

                request.setFullName("Public Registration User");
                request.setEmail("publicregister@gmail.com");
                request.setPassword("Password@123");
                request.setPhone("5555555555");

                mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                request)))
                                .andExpect(status().isCreated());
        }

        // ============================================================
        // LOGIN - PUBLIC API
        // ============================================================

        @Test
        void loginUser_shouldBeAccessibleWithoutAuthentication()
                        throws Exception {

                // Register user first
                RegisterRequest registerRequest = new RegisterRequest();

                registerRequest.setFullName("Public Login User");
                registerRequest.setEmail("publiclogin@gmail.com");
                registerRequest.setPassword("Password@123");
                registerRequest.setPhone("4444444444");

                mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                registerRequest)))
                                .andExpect(status().isCreated());

                // Login without JWT
                LoginRequest loginRequest = new LoginRequest();

                loginRequest.setEmail("publiclogin@gmail.com");
                loginRequest.setPassword("Password@123");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                loginRequest)))
                                .andExpect(status().isOk());
        }

        // Hi allow

}