
package com.example.demo.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthApiController.class)
class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    /*
     * These beans may be required because AuthApiControllerTest
     * loads the Spring MVC test context together with your
     * application's security configuration.
     */
    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // =========================================================
    // REGISTER - SUCCESS
    // =========================================================

    @Test
    void registerUser_shouldReturn201_whenRequestIsValid()
            throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setFullName("Nagaraja");
        request.setEmail("nagaraja@gmail.com");
        request.setPassword("Password@123");
        request.setPhone("9876543210");

        doNothing()
                .when(authService)
                .register(any(RegisterRequest.class));

        mockMvc.perform(
                post("/api/auth/register")
                        .with(user("user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(
                authService,
                times(1))
                .register(any(RegisterRequest.class));
    }

    // =========================================================
    // LOGIN - SUCCESS
    // =========================================================

    @Test
    void loginUser_shouldReturn200_whenCredentialsAreValid()
            throws Exception {

        LoginRequest request = new LoginRequest();

        request.setEmail("nagaraja@gmail.com");
        request.setPassword("Password@123");

        LoginResponse response = new LoginResponse("mockJwtToken", new UserResponse());

        when(
                authService.login(
                        any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/login")
                        .with(user("user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(
                authService,
                times(1))
                .login(any(LoginRequest.class));
    }

    // =========================================================
    // REGISTER - INVALID REQUEST
    // =========================================================

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
                        .with(user("user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        /*
         * Because validation fails at the controller level,
         * service should not be called.
         */
        verify(
                authService,
                times(0))
                .register(any(RegisterRequest.class));
    }

    // =========================================================
    // LOGIN - INVALID REQUEST
    // =========================================================

    @Test
    void loginUser_shouldReturn400_whenRequestIsInvalid()
            throws Exception {

        LoginRequest request = new LoginRequest();

        request.setEmail("");
        request.setPassword("");

        when(
                authService.login(
                        any(LoginRequest.class)))
                .thenThrow(
                        new InvalidRequestException(
                                "Email and password are required."));

        mockMvc.perform(
                post("/api/auth/login")
                        .with(user("user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(
                authService,
                times(1))
                .login(any(LoginRequest.class));
    }

    // =========================================================
    // LOGIN - INVALID CREDENTIALS
    // =========================================================

    @Test
    void loginUser_shouldReturn400_whenCredentialsAreInvalid()
            throws Exception {

        LoginRequest request = new LoginRequest();

        request.setEmail("wrong@gmail.com");
        request.setPassword("WrongPassword");

        when(
                authService.login(
                        any(LoginRequest.class)))
                .thenThrow(
                        new InvalidRequestException(
                                "Invalid email or password."));

        mockMvc.perform(
                post("/api/auth/login")
                        .with(user("user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(
                authService,
                times(1))
                .login(any(LoginRequest.class));
    }

    // =========================================================
    // REGISTER - SERVICE EXCEPTION
    // =========================================================

    @Test
    void registerUser_shouldReturnError_whenRegistrationFails()
            throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setFullName("Nagaraja");
        request.setEmail("existing@gmail.com");
        request.setPassword("Password@123");
        request.setPhone("9876543210");

        doThrow(
                new DuplicateResourceException(
                        "Email address is already registered."))
                .when(authService)
                .register(any(RegisterRequest.class));

        mockMvc.perform(
                post("/api/auth/register")
                        .with(user("user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(
                authService,
                times(1))
                .register(any(RegisterRequest.class));
    }

    // =========================================================
    // REGISTER - WRONG HTTP METHOD
    // =========================================================

    @Test
    void registerUser_shouldReturn405_whenUsingGet()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/register")
                        .with(user("user")))
                .andExpect(status().isMethodNotAllowed());
    }

    // =========================================================
    // LOGIN - WRONG HTTP METHOD
    // =========================================================

    @Test
    void loginUser_shouldReturn405_whenUsingGet()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/login")
                        .with(user("user")))
                .andExpect(status().isMethodNotAllowed());
    }

    // =========================================================
    // REGISTER - CONTENT TYPE
    // =========================================================

    @Test
    void registerUser_shouldReturn415_whenContentTypeIsUnsupported()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/register")
                        .with(user("user"))
                        .with(csrf())
                        .contentType(
                                MediaType.TEXT_PLAIN)
                        .content("invalid request"))
                .andExpect(status().isUnsupportedMediaType());

        verify(
                authService,
                times(0))
                .register(any(RegisterRequest.class));
    }

    // =========================================================
    // LOGIN - CONTENT TYPE
    // =========================================================

    @Test
    void loginUser_shouldReturn415_whenContentTypeIsUnsupported()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/login")
                        .with(user("user"))
                        .with(csrf())
                        .contentType(
                                MediaType.TEXT_PLAIN)
                        .content("invalid request"))
                .andExpect(status().isUnsupportedMediaType());

        verify(
                authService,
                times(0))
                .login(any(LoginRequest.class));
    }
}
