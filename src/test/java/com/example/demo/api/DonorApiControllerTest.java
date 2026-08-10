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

import com.example.demo.dto.request.DonorRequest;
import com.example.demo.dto.response.DonorResponse;
import com.example.demo.enums.BloodGroup;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.DonorService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(DonorApiController.class)
@ActiveProfiles("test")
class DonorApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DonorService donorService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // =========================================================
    // GET ALL DONORS - SUCCESS
    // =========================================================

    @Test
    void getAllDonors_shouldReturn200_whenDonorsExist() throws Exception {

        DonorResponse donor1 = new DonorResponse();
        donor1.setId(1L);
        donor1.setFullName("John Doe");
        donor1.setBloodGroup(BloodGroup.A_POSITIVE);

        DonorResponse donor2 = new DonorResponse();
        donor2.setId(2L);
        donor2.setFullName("Jane Smith");
        donor2.setBloodGroup(BloodGroup.O_NEGATIVE);

        List<DonorResponse> donors = Arrays.asList(donor1, donor2);

        when(donorService.getAllDonors()).thenReturn(donors);

        mockMvc.perform(get("/api/donors")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].fullName").value("Jane Smith"));

        verify(donorService, times(1)).getAllDonors();
    }

    // =========================================================
    // GET ALL DONORS - EMPTY LIST
    // =========================================================

    @Test
    void getAllDonors_shouldReturn200_whenNoDonorsExist() throws Exception {

        when(donorService.getAllDonors()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/donors")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(donorService, times(1)).getAllDonors();
    }

    // =========================================================
    // GET DONOR BY ID - SUCCESS
    // =========================================================

    @Test
    void getDonorById_shouldReturn200_whenDonorExists() throws Exception {

        Long donorId = 1L;

        DonorResponse response = new DonorResponse();
        response.setId(donorId);
        response.setFullName("John Doe");
        response.setEmail("john@example.com");

        when(donorService.getDonorById(donorId)).thenReturn(response);

        mockMvc.perform(get("/api/donors/{id}", donorId)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(donorService, times(1)).getDonorById(donorId);
    }

    // =========================================================
    // GET DONOR BY ID - NOT FOUND
    // =========================================================

    @Test
    void getDonorById_shouldReturn404_whenDonorDoesNotExist() throws Exception {

        Long donorId = 999L;

        when(donorService.getDonorById(donorId))
                .thenThrow(new RuntimeException("Donor not found with ID: " + donorId));

        mockMvc.perform(get("/api/donors/{id}", donorId)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());

        verify(donorService, times(1)).getDonorById(donorId);
    }

    // =========================================================
    // GET DONOR BY ID - INVALID ID
    // =========================================================

    @Test
    void getDonorById_shouldReturn400_whenIdIsInvalid() throws Exception {

        mockMvc.perform(get("/api/donors/abc")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        verify(donorService, times(0)).getDonorById(any());
    }

    // =========================================================
    // ADD DONOR - SUCCESS
    // =========================================================

    @Test
    void addDonor_shouldReturn201_whenAdditionIsSuccessful() throws Exception {

        DonorRequest request = new DonorRequest();
        request.setEmail("john@example.com");
        request.setCity("Bangalore");
        request.setBloodGroup(BloodGroup.O_POSITIVE);

        doNothing().when(donorService).saveDonor(any(DonorRequest.class));

        mockMvc.perform(post("/api/donors")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Donor Added Successfully"));

        verify(donorService, times(1)).saveDonor(any(DonorRequest.class));
    }

    // =========================================================
    // UPDATE DONOR - SUCCESS
    // =========================================================

    @Test
    void updateDonor_shouldReturn200_whenUpdateIsSuccessful() throws Exception {

        Long donorId = 1L;

        DonorRequest request = new DonorRequest();
        request.setEmail("john_updated@example.com");
        request.setCity("Bangalore");

        doNothing().when(donorService).updateDonorById(eq(donorId), any(DonorRequest.class));

        mockMvc.perform(put("/api/donors/{id}", donorId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Donor Updated Successfully"));

        verify(donorService, times(1)).updateDonorById(eq(donorId), any(DonorRequest.class));
    }

    // =========================================================
    // UPDATE DONOR - NOT FOUND
    // =========================================================

    @Test
    void updateDonor_shouldReturn404_whenDonorDoesNotExist() throws Exception {

        Long donorId = 999L;

        DonorRequest request = new DonorRequest();

        doThrow(new RuntimeException("Donor not found with ID: " + donorId))
                .when(donorService).updateDonorById(eq(donorId), any(DonorRequest.class));

        mockMvc.perform(put("/api/donors/{id}", donorId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(donorService, times(1)).updateDonorById(eq(donorId), any(DonorRequest.class));
    }

    // =========================================================
    // DELETE DONOR - SUCCESS
    // =========================================================

    @Test
    void deleteDonor_shouldReturn200_whenDeleteIsSuccessful() throws Exception {

        Long donorId = 1L;

        doNothing().when(donorService).deleteDonorById(donorId);

        mockMvc.perform(delete("/api/donors/{id}", donorId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Donor Deleted Successfully"));

        verify(donorService, times(1)).deleteDonorById(donorId);
    }

    // =========================================================
    // DELETE DONOR - NOT FOUND
    // =========================================================

    @Test
    void deleteDonor_shouldReturn404_whenDonorDoesNotExist() throws Exception {

        Long donorId = 999L;

        doThrow(new RuntimeException("Donor not found with ID: " + donorId))
                .when(donorService).deleteDonorById(donorId);

        mockMvc.perform(delete("/api/donors/{id}", donorId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(donorService, times(1)).deleteDonorById(donorId);
    }
}
