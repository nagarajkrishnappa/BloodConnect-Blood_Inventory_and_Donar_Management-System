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

import com.example.demo.dto.request.BloodRequestRequest;
import com.example.demo.dto.response.BloodRequestResponse;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.RequestStatus;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.BloodRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BloodRequestApiController.class)
class BloodRequestApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BloodRequestService bloodRequestService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // =========================================================
    // GET ALL REQUESTS - SUCCESS
    // =========================================================

    @Test
    void getAllRequests_shouldReturn200_whenRequestsExist() throws Exception {

        BloodRequestResponse req1 = new BloodRequestResponse();
        req1.setId(1L);
        req1.setFullName("John Doe");
        req1.setBloodGroup(BloodGroup.A_POSITIVE);
        req1.setStatus(RequestStatus.PENDING);

        BloodRequestResponse req2 = new BloodRequestResponse();
        req2.setId(2L);
        req2.setFullName("Jane Smith");
        req2.setBloodGroup(BloodGroup.O_NEGATIVE);
        req2.setStatus(RequestStatus.APPROVED);

        List<BloodRequestResponse> requests = Arrays.asList(req1, req2);

        when(bloodRequestService.getAllRequests()).thenReturn(requests);

        mockMvc.perform(get("/api/bloodrequests")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].fullName").value("Jane Smith"));

        verify(bloodRequestService, times(1)).getAllRequests();
    }

    // =========================================================
    // GET ALL REQUESTS - EMPTY LIST
    // =========================================================

    @Test
    void getAllRequests_shouldReturn200_whenNoRequestsExist() throws Exception {

        when(bloodRequestService.getAllRequests()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bloodrequests")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(bloodRequestService, times(1)).getAllRequests();
    }

    // =========================================================
    // GET REQUEST BY ID - SUCCESS
    // =========================================================

    @Test
    void getRequestById_shouldReturn200_whenRequestExists() throws Exception {

        Long requestId = 1L;

        BloodRequestResponse response = new BloodRequestResponse();
        response.setId(requestId);
        response.setFullName("John Doe");
        response.setReason("Urgent Surgery");

        when(bloodRequestService.getRequestById(requestId)).thenReturn(response);

        mockMvc.perform(get("/api/bloodrequests/{id}", requestId)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.reason").value("Urgent Surgery"));

        verify(bloodRequestService, times(1)).getRequestById(requestId);
    }

    // =========================================================
    // GET REQUEST BY ID - NOT FOUND
    // =========================================================

    @Test
    void getRequestById_shouldReturn404_whenRequestDoesNotExist() throws Exception {

        Long requestId = 999L;

        when(bloodRequestService.getRequestById(requestId))
                .thenThrow(new RuntimeException("Blood request not found with ID: " + requestId));

        mockMvc.perform(get("/api/bloodrequests/{id}", requestId)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());

        verify(bloodRequestService, times(1)).getRequestById(requestId);
    }

    // =========================================================
    // GET REQUEST BY ID - INVALID ID
    // =========================================================

    @Test
    void getRequestById_shouldReturn400_whenIdIsInvalid() throws Exception {

        mockMvc.perform(get("/api/bloodrequests/abc")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        verify(bloodRequestService, times(0)).getRequestById(any());
    }

    // =========================================================
    // CREATE BLOOD REQUEST - SUCCESS
    // =========================================================

    @Test
    void createRequest_shouldReturn201_whenCreationIsSuccessful() throws Exception {

        BloodRequestRequest request = new BloodRequestRequest();
        request.setEmail("john@example.com");
        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setUnitsRequired(2);
        request.setReason("Emergency");

        doNothing().when(bloodRequestService).saveRequest(any(BloodRequestRequest.class));

        mockMvc.perform(post("/api/bloodrequests")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Blood Request Submitted Successfully"));

        verify(bloodRequestService, times(1)).saveRequest(any(BloodRequestRequest.class));
    }

    // =========================================================
    // APPROVE REQUEST - SUCCESS
    // =========================================================

    @Test
    void approveRequest_shouldReturn200_whenApprovalIsSuccessful() throws Exception {

        Long requestId = 1L;

        doNothing().when(bloodRequestService).approveRequest(requestId);

        mockMvc.perform(put("/api/bloodrequests/{id}/approve", requestId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Blood Request Approved Successfully"));

        verify(bloodRequestService, times(1)).approveRequest(requestId);
    }

    // =========================================================
    // APPROVE REQUEST - NOT FOUND
    // =========================================================

    @Test
    void approveRequest_shouldReturn404_whenRequestDoesNotExist() throws Exception {

        Long requestId = 999L;

        doThrow(new RuntimeException("Blood request not found"))
                .when(bloodRequestService).approveRequest(requestId);

        mockMvc.perform(put("/api/bloodrequests/{id}/approve", requestId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bloodRequestService, times(1)).approveRequest(requestId);
    }

    // =========================================================
    // REJECT REQUEST - SUCCESS
    // =========================================================

    @Test
    void rejectRequest_shouldReturn200_whenRejectionIsSuccessful() throws Exception {

        Long requestId = 1L;

        doNothing().when(bloodRequestService).rejectRequest(requestId);

        mockMvc.perform(put("/api/bloodrequests/{id}/reject", requestId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Blood Request Rejected Successfully"));

        verify(bloodRequestService, times(1)).rejectRequest(requestId);
    }

    // =========================================================
    // REJECT REQUEST - NOT FOUND
    // =========================================================

    @Test
    void rejectRequest_shouldReturn404_whenRequestDoesNotExist() throws Exception {

        Long requestId = 999L;

        doThrow(new RuntimeException("Blood request not found"))
                .when(bloodRequestService).rejectRequest(requestId);

        mockMvc.perform(put("/api/bloodrequests/{id}/reject", requestId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bloodRequestService, times(1)).rejectRequest(requestId);
    }
}
