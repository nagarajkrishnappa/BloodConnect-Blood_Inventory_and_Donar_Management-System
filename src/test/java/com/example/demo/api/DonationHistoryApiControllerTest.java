
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

import com.example.demo.dto.request.DonationHistoryRequest;
import com.example.demo.dto.response.DonationHistoryResponse;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.DonationHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(DonationHistoryApiController.class)
@ActiveProfiles("test")
class DonationHistoryApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DonationHistoryService donationHistoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // =========================================================
    // GET ALL DONATIONS - SUCCESS
    // =========================================================

    @Test
    void getAllDonations_shouldReturn200_whenDonationsExist()
            throws Exception {

        DonationHistoryResponse donation1 = new DonationHistoryResponse();

        DonationHistoryResponse donation2 = new DonationHistoryResponse();

        List<DonationHistoryResponse> donations = Arrays.asList(donation1, donation2);

        when(donationHistoryService.getAllDonations())
                .thenReturn(donations);

        mockMvc.perform(
                get("/api/donations")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.length()").value(2));

        verify(
                donationHistoryService,
                times(1))
                .getAllDonations();
    }

    // =========================================================
    // GET ALL DONATIONS - EMPTY
    // =========================================================

    @Test
    void getAllDonations_shouldReturn200_whenNoDonationsExist()
            throws Exception {

        when(donationHistoryService.getAllDonations())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                get("/api/donations")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.length()").value(0));

        verify(
                donationHistoryService,
                times(1))
                .getAllDonations();
    }

    // =========================================================
    // GET DONATION BY ID - SUCCESS
    // =========================================================

    @Test
    void getDonationById_shouldReturn200_whenDonationExists()
            throws Exception {

        Long donationId = 1L;

        DonationHistoryResponse response = new DonationHistoryResponse();

        when(
                donationHistoryService
                        .getDonationById(donationId))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/donations/{id}", donationId)
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON));

        verify(
                donationHistoryService,
                times(1))
                .getDonationById(donationId);
    }

    // =========================================================
    // GET DONATION BY ID - NOT FOUND
    // =========================================================

    @Test
    void getDonationById_shouldReturn404_whenDonationDoesNotExist()
            throws Exception {

        Long donationId = 999L;

        when(
                donationHistoryService
                        .getDonationById(donationId))
                .thenThrow(
                        new RuntimeException(
                                "Donation history record not found with ID: "
                                        + donationId));

        mockMvc.perform(
                get("/api/donations/{id}", donationId)
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isNotFound());

        verify(
                donationHistoryService,
                times(1))
                .getDonationById(donationId);
    }

    // =========================================================
    // ADD DONATION - SUCCESS
    // =========================================================

    @Test
    void addDonation_shouldReturn201_whenRequestIsValid()
            throws Exception {

        DonationHistoryRequest request = new DonationHistoryRequest();

        doNothing()
                .when(donationHistoryService)
                .saveDonation(
                        any(DonationHistoryRequest.class));

        mockMvc.perform(
                post("/api/donations")
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
                                "Donation Recorded Successfully"));

        verify(
                donationHistoryService,
                times(1))
                .saveDonation(
                        any(DonationHistoryRequest.class));
    }

    // =========================================================
    // ADD DONATION - SERVICE FAILURE
    // =========================================================

    @Test
    void addDonation_shouldReturn404_whenDonorDoesNotExist()
            throws Exception {

        DonationHistoryRequest request = new DonationHistoryRequest();

        doThrow(
                new RuntimeException(
                        "Donor not found"))
                .when(donationHistoryService)
                .saveDonation(
                        any(DonationHistoryRequest.class));

        mockMvc.perform(
                post("/api/donations")
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
                donationHistoryService,
                times(1))
                .saveDonation(
                        any(DonationHistoryRequest.class));
    }

    // =========================================================
    // UPDATE DONATION - SUCCESS
    // =========================================================

    @Test
    void updateDonation_shouldReturn200_whenUpdateIsSuccessful()
            throws Exception {

        Long donationId = 1L;

        DonationHistoryRequest request = new DonationHistoryRequest();

        doNothing()
                .when(donationHistoryService)
                .updateDonation(
                        eq(donationId),
                        any(DonationHistoryRequest.class));

        mockMvc.perform(
                put("/api/donations/{id}", donationId)
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
                                "Donation Updated Successfully"));

        verify(
                donationHistoryService,
                times(1))
                .updateDonation(
                        eq(donationId),
                        any(DonationHistoryRequest.class));
    }

    // =========================================================
    // UPDATE DONATION - NOT FOUND
    // =========================================================

    @Test
    void updateDonation_shouldReturn404_whenDonationDoesNotExist()
            throws Exception {

        Long donationId = 999L;

        DonationHistoryRequest request = new DonationHistoryRequest();

        doThrow(
                new RuntimeException(
                        "Donation history record not found with ID: "
                                + donationId))
                .when(donationHistoryService)
                .updateDonation(
                        eq(donationId),
                        any(DonationHistoryRequest.class));

        mockMvc.perform(
                put("/api/donations/{id}", donationId)
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
                donationHistoryService,
                times(1))
                .updateDonation(
                        eq(donationId),
                        any(DonationHistoryRequest.class));
    }

    // =========================================================
    // DELETE DONATION - SUCCESS
    // =========================================================

    @Test
    void deleteDonation_shouldReturn200_whenDeleteIsSuccessful()
            throws Exception {

        Long donationId = 1L;

        doNothing()
                .when(donationHistoryService)
                .deleteDonation(donationId);

        mockMvc.perform(
                delete("/api/donations/{id}", donationId)
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "Donation Deleted Successfully"));

        verify(
                donationHistoryService,
                times(1))
                .deleteDonation(donationId);
    }

    // =========================================================
    // DELETE DONATION - NOT FOUND
    // =========================================================

    @Test
    void deleteDonation_shouldReturn404_whenDonationDoesNotExist()
            throws Exception {

        Long donationId = 999L;

        doThrow(
                new RuntimeException(
                        "Donation history record not found with ID: "
                                + donationId))
                .when(donationHistoryService)
                .deleteDonation(donationId);

        mockMvc.perform(
                delete("/api/donations/{id}", donationId)
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(
                donationHistoryService,
                times(1))
                .deleteDonation(donationId);
    }

    // =========================================================
    // INVALID ID
    // =========================================================

    @Test
    void getDonationById_shouldReturn400_whenIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                get("/api/donations/abc")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isBadRequest());

        verify(
                donationHistoryService,
                times(0))
                .getDonationById(any());
    }

    // =========================================================
    // WRONG HTTP METHOD
    // =========================================================

    @Test
    void getDonation_shouldReturn405_whenUsingPostOnIdEndpoint()
            throws Exception {

        mockMvc.perform(
                post("/api/donations/1")
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }

    // =========================================================
    // NO AUTHENTICATION
    // =========================================================

    @Test
    void getDonations_shouldReturn401_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/donations"))
                .andExpect(status().isUnauthorized());

        verify(
                donationHistoryService,
                times(0))
                .getAllDonations();
    }

    // =========================================================
    // AUTHENTICATED USER
    // =========================================================

    @Test
    void getDonations_shouldAllowAuthenticatedUser()
            throws Exception {

        when(donationHistoryService.getAllDonations())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                get("/api/donations")
                        .with(user("user")
                                .roles("USER")))
                .andExpect(status().isOk());

        verify(
                donationHistoryService,
                times(1))
                .getAllDonations();
    }

    // =========================================================
    // AUTHENTICATED ADMIN
    // =========================================================

    @Test
    void getDonations_shouldAllowAuthenticatedAdmin()
            throws Exception {

        when(donationHistoryService.getAllDonations())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                get("/api/donations")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk());

        verify(
                donationHistoryService,
                times(1))
                .getAllDonations();
    }
}
