
package com.example.demo.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserApiController.class)
class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // =========================================================
    // GET ALL USERS - SUCCESS
    // =========================================================

    @Test
    void getAllUsers_shouldReturn200_whenUsersExist()
            throws Exception {

        UserResponse user1 = new UserResponse();
        UserResponse user2 = new UserResponse();

        List<UserResponse> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers())
                .thenReturn(users);

        mockMvc.perform(
                get("/api/users")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService, times(1))
                .getAllUsers();
    }

    // =========================================================
    // GET ALL USERS - EMPTY LIST
    // =========================================================

    @Test
    void getAllUsers_shouldReturn200_whenNoUsersExist()
            throws Exception {

        when(userService.getAllUsers())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                get("/api/users")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1))
                .getAllUsers();
    }

    // =========================================================
    // GET USER BY ID - SUCCESS
    // =========================================================

    @Test
    void getUserById_shouldReturn200_whenUserExists()
            throws Exception {

        Long userId = 1L;

        UserResponse response = new UserResponse();

        when(userService.getUserById(userId))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON));

        verify(userService, times(1))
                .getUserById(userId);
    }

    // =========================================================
    // GET USER BY ID - NOT FOUND
    // =========================================================

    @Test
    void getUserById_shouldReturn404_whenUserDoesNotExist()
            throws Exception {

        Long userId = 999L;

        when(userService.getUserById(userId))
                .thenThrow(
                        new RuntimeException("User not found"));

        mockMvc.perform(
                get("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());

        verify(userService, times(1))
                .getUserById(userId);
    }

    // =========================================================
    // UPDATE USER - SUCCESS
    // =========================================================

    @Test
    void updateUser_shouldReturn200_whenUpdateIsSuccessful()
            throws Exception {

        Long userId = 1L;

        UserUpdateRequest request = new UserUpdateRequest();

        doNothing()
                .when(userService)
                .updateUser(
                        eq(userId),
                        any(UserUpdateRequest.class));

        mockMvc.perform(
                put("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "User Updated Successfully"));

        verify(userService, times(1))
                .updateUser(
                        eq(userId),
                        any(UserUpdateRequest.class));
    }

    // =========================================================
    // UPDATE USER - NOT FOUND
    // =========================================================

    @Test
    void updateUser_shouldReturn404_whenUserDoesNotExist()
            throws Exception {

        Long userId = 999L;

        UserUpdateRequest request = new UserUpdateRequest();

        doThrow(
                new RuntimeException("User not found"))
                .when(userService)
                .updateUser(
                        eq(userId),
                        any(UserUpdateRequest.class));

        mockMvc.perform(
                put("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(userService, times(1))
                .updateUser(
                        eq(userId),
                        any(UserUpdateRequest.class));
    }

    // =========================================================
    // DELETE USER - SUCCESS
    // =========================================================

    @Test
    void deleteUser_shouldReturn200_whenDeleteIsSuccessful()
            throws Exception {

        Long userId = 1L;

        doNothing()
                .when(userService)
                .deleteUser(userId);

        mockMvc.perform(
                delete("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "User Deleted Successfully"));

        verify(userService, times(1))
                .deleteUser(userId);
    }

    // =========================================================
    // DELETE USER - NOT FOUND
    // =========================================================

    @Test
    void deleteUser_shouldReturn404_whenUserDoesNotExist()
            throws Exception {

        Long userId = 999L;

        doThrow(
                new RuntimeException("User not found"))
                .when(userService)
                .deleteUser(userId);

        mockMvc.perform(
                delete("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService, times(1))
                .deleteUser(userId);
    }

    // =========================================================
    // GET USER BY ID - INVALID ID
    // =========================================================

    @Test
    void getUserById_shouldReturn400_whenIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                get("/api/users/abc")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        verify(userService, times(0))
                .getUserById(any());
    }

    // =========================================================
    // WRONG HTTP METHOD
    // =========================================================

    @Test
    void createUser_shouldReturn405_whenPostIsUsed()
            throws Exception {

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }

    // =========================================================
    // PROTECTED API - NOT AUTHENTICATED
    // =========================================================

    @Test
    void getAllUsers_shouldReturn401_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/users"))
                .andExpect(status().isUnauthorized());

        verify(userService, times(0))
                .getAllUsers();
    }

    // =========================================================
    // PROTECTED API - AUTHENTICATED USER
    // =========================================================

    @Test
    void getAllUsers_shouldReachController_whenAuthenticated()
            throws Exception {

        when(userService.getAllUsers())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                get("/api/users")
                        .with(user("testuser")
                                .roles("USER")))
                .andExpect(status().isOk());

        verify(userService, times(1))
                .getAllUsers();
    }

    // =========================================================
    // PROTECTED API - AUTHENTICATED ADMIN
    // =========================================================

    @Test
    void getAllUsers_shouldReachController_whenAdmin()
            throws Exception {

        when(userService.getAllUsers())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                get("/api/users")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk());

        verify(userService, times(1))
                .getAllUsers();
    }
}
