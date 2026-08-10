
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.request.BloodStockRequest;
import com.example.demo.dto.response.BloodStockResponse;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.BloodStockService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(BloodStockApiController.class)
@ActiveProfiles("test")
class BloodStockApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BloodStockService bloodStockService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // =========================================================
    // GET ALL BLOOD STOCK - SUCCESS
    // =========================================================

    @Test
    void getAllBloodStock_shouldReturn200_whenStockExists()
            throws Exception {

        BloodStockResponse stock1 = new BloodStockResponse();
        BloodStockResponse stock2 = new BloodStockResponse();

        List<BloodStockResponse> stocks = Arrays.asList(stock1, stock2);

        when(bloodStockService.getAllBloodStock())
                .thenReturn(stocks);

        mockMvc.perform(
                get("/api/bloodstock")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.length()").value(2));

        verify(bloodStockService, times(1))
                .getAllBloodStock();
    }

    // =========================================================
    // GET ALL BLOOD STOCK - EMPTY
    // =========================================================

    @Test
    void getAllBloodStock_shouldReturn200_whenStockIsEmpty()
            throws Exception {

        when(bloodStockService.getAllBloodStock())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                get("/api/bloodstock")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.length()").value(0));

        verify(bloodStockService, times(1))
                .getAllBloodStock();
    }

    // =========================================================
    // GET BLOOD STOCK BY ID - SUCCESS
    // =========================================================

    @Test
    void getBloodStockById_shouldReturn200_whenStockExists()
            throws Exception {

        Long stockId = 1L;

        BloodStockResponse response = new BloodStockResponse();

        when(
                bloodStockService
                        .getBloodStockResponseById(stockId))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/bloodstock/{id}", stockId)
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON));

        verify(
                bloodStockService,
                times(1))
                .getBloodStockResponseById(stockId);
    }

    // =========================================================
    // GET BLOOD STOCK BY ID - NOT FOUND
    // =========================================================

    @Test
    void getBloodStockById_shouldReturn404_whenStockDoesNotExist()
            throws Exception {

        Long stockId = 999L;

        when(
                bloodStockService
                        .getBloodStockResponseById(stockId))
                .thenThrow(
                        new RuntimeException(
                                "Blood Stock Not Found"));

        mockMvc.perform(
                get("/api/bloodstock/{id}", stockId)
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isNotFound());

        verify(
                bloodStockService,
                times(1))
                .getBloodStockResponseById(stockId);
    }

    // =========================================================
    // ADD BLOOD STOCK - SUCCESS
    // =========================================================

    @Test
    void addBloodStock_shouldReturn201_whenRequestIsValid()
            throws Exception {

        BloodStockRequest request = new BloodStockRequest();

        doNothing()
                .when(bloodStockService)
                .saveBloodStock(
                        any(BloodStockRequest.class));

        mockMvc.perform(
                post("/api/bloodstock")
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf())
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        request)))
                .andExpect(status().isCreated())
                .andExpect(
                        content().string(
                                "Blood Stock Added Successfully"));

        verify(
                bloodStockService,
                times(1))
                .saveBloodStock(
                        any(BloodStockRequest.class));
    }

    // =========================================================
    // ADD BLOOD STOCK - FAILURE
    // =========================================================

    @Test
    void addBloodStock_shouldReturn409_whenStockAlreadyExists()
            throws Exception {

        BloodStockRequest request = new BloodStockRequest();

        doThrow(
                new RuntimeException(
                        "Blood group already exists."))
                .when(bloodStockService)
                .saveBloodStock(
                        any(BloodStockRequest.class));

        mockMvc.perform(
                post("/api/bloodstock")
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf())
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        request)))
                .andExpect(status().isInternalServerError());

        verify(
                bloodStockService,
                times(1))
                .saveBloodStock(
                        any(BloodStockRequest.class));
    }

    // =========================================================
    // UPDATE BLOOD STOCK - SUCCESS
    // =========================================================

    @Test
    void updateBloodStock_shouldReturn200_whenUpdateIsSuccessful()
            throws Exception {

        Long stockId = 1L;

        BloodStockRequest request = new BloodStockRequest();

        doNothing()
                .when(bloodStockService)
                .updateBloodStock(
                        eq(stockId),
                        any(BloodStockRequest.class));

        mockMvc.perform(
                put("/api/bloodstock/{id}", stockId)
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf())
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        request)))
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "Blood Stock Updated Successfully"));

        verify(
                bloodStockService,
                times(1))
                .updateBloodStock(
                        eq(stockId),
                        any(BloodStockRequest.class));
    }

    // =========================================================
    // UPDATE BLOOD STOCK - NOT FOUND
    // =========================================================

    @Test
    void updateBloodStock_shouldReturn404_whenStockDoesNotExist()
            throws Exception {

        Long stockId = 999L;

        BloodStockRequest request = new BloodStockRequest();

        doThrow(
                new RuntimeException(
                        "Blood stock not found."))
                .when(bloodStockService)
                .updateBloodStock(
                        eq(stockId),
                        any(BloodStockRequest.class));

        mockMvc.perform(
                put("/api/bloodstock/{id}", stockId)
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf())
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        request)))
                .andExpect(status().isNotFound());

        verify(
                bloodStockService,
                times(1))
                .updateBloodStock(
                        eq(stockId),
                        any(BloodStockRequest.class));
    }

    // =========================================================
    // DELETE BLOOD STOCK - SUCCESS
    // =========================================================

    @Test
    void deleteBloodStock_shouldReturn200_whenDeleteIsSuccessful()
            throws Exception {

        Long stockId = 1L;

        doNothing()
                .when(bloodStockService)
                .deleteBloodStock(stockId);

        mockMvc.perform(
                delete("/api/bloodstock/{id}", stockId)
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "Blood Stock Deleted Successfully"));

        verify(
                bloodStockService,
                times(1))
                .deleteBloodStock(stockId);
    }

    // =========================================================
    // DELETE BLOOD STOCK - NOT FOUND
    // =========================================================

    @Test
    void deleteBloodStock_shouldReturn404_whenStockDoesNotExist()
            throws Exception {

        Long stockId = 999L;

        doThrow(
                new RuntimeException(
                        "Blood Stock Not Found"))
                .when(bloodStockService)
                .deleteBloodStock(stockId);

        mockMvc.perform(
                delete("/api/bloodstock/{id}", stockId)
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(
                bloodStockService,
                times(1))
                .deleteBloodStock(stockId);
    }

    // =========================================================
    // INVALID ID
    // =========================================================

    @Test
    void getBloodStockById_shouldReturn400_whenIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodstock/abc")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isBadRequest());

        verify(
                bloodStockService,
                times(0))
                .getBloodStockResponseById(any());
    }

    // =========================================================
    // WRONG HTTP METHOD
    // =========================================================

    @Test
    void getBloodStock_shouldReturn405_whenUsingPostWithoutBody()
            throws Exception {

        mockMvc.perform(
                post("/api/bloodstock/1")
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }

    // =========================================================
    // NO AUTHENTICATION
    // =========================================================

    @Test
    void getBloodStock_shouldReturn401_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodstock"))
                .andExpect(status().isUnauthorized());

        verify(
                bloodStockService,
                times(0))
                .getAllBloodStock();
    }

    // =========================================================
    // AUTHENTICATED USER
    // =========================================================

    @Test
    void getBloodStock_shouldAllowAuthenticatedUser()
            throws Exception {

        when(bloodStockService.getAllBloodStock())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                get("/api/bloodstock")
                        .with(user("user")
                                .roles("USER")))
                .andExpect(status().isOk());

        verify(
                bloodStockService,
                times(1))
                .getAllBloodStock();
    }

    // =========================================================
    // AUTHENTICATED ADMIN
    // =========================================================

    @Test
    void getBloodStock_shouldAllowAuthenticatedAdmin()
            throws Exception {

        when(bloodStockService.getAllBloodStock())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                get("/api/bloodstock")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk());

        verify(
                bloodStockService,
                times(1))
                .getAllBloodStock();
    }
}
