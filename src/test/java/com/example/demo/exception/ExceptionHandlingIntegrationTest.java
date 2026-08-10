package com.example.demo.exception;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExceptionHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ============================================================
    // 1. RESOURCE NOT FOUND - DONOR
    // ============================================================

    @Test
    void getDonorById_shouldReturn404_whenDonorDoesNotExist()
            throws Exception {

        mockMvc.perform(
                get("/api/donors/999999")
                        .with(user("testuser")
                                .roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"));
    }

    // ============================================================
    // 2. RESOURCE NOT FOUND - BLOOD STOCK
    // ============================================================

    @Test
    void getBloodStockById_shouldReturn404_whenStockDoesNotExist()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodstock/999999")
                        .with(user("testuser")
                                .roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"));
    }

    // ============================================================
    // 3. RESOURCE NOT FOUND - BLOOD REQUEST
    // ============================================================

    @Test
    void getBloodRequestById_shouldReturn404_whenRequestDoesNotExist()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodrequests/999999")
                        .with(user("testuser")
                                .roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"));
    }

    // ============================================================
    // 4. RESOURCE NOT FOUND - DONATION
    // ============================================================

    @Test
    void getDonationById_shouldReturn404_whenDonationDoesNotExist()
            throws Exception {

        mockMvc.perform(
                get("/api/donations/999999")
                        .with(user("testuser")
                                .roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"));
    }

    // ============================================================
    // 5. METHOD NOT SUPPORTED
    // ============================================================

    @Test
    void donorEndpoint_shouldReturn405_whenWrongHttpMethodIsUsed()
            throws Exception {

        mockMvc.perform(
                post("/api/donors/1")
                        .with(user("testuser")
                                .roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error")
                        .value("Method Not Allowed"));
    }

    // ============================================================
    // 6. METHOD NOT SUPPORTED - BLOOD STOCK
    // ============================================================

    @Test
    void bloodStockEndpoint_shouldReturn405_whenWrongHttpMethodIsUsed()
            throws Exception {

        mockMvc.perform(
                post("/api/bloodstock/1")
                        .with(user("admin")
                                .roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error")
                        .value("Method Not Allowed"));
    }

    // ============================================================
    // 7. INVALID PATH VARIABLE TYPE
    // ============================================================

    @Test
    void donorEndpoint_shouldReturn400_whenIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                get("/api/donors/abc")
                        .with(user("testuser")
                                .roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"));
    }

    // ============================================================
    // 8. INVALID CONTENT TYPE
    // ============================================================

    @Test
    void addDonor_shouldReturn415_whenContentTypeIsInvalid()
            throws Exception {

        mockMvc.perform(
                post("/api/donors")
                        .with(user("testuser")
                                .roles("USER"))
                        .content("invalid content")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error")
                        .value("Unsupported Media Type"));
    }

    // ============================================================
    // 9. UNAUTHENTICATED REQUEST
    // ============================================================

    @Test
    void donorEndpoint_shouldReturn401_whenUserIsNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/donors")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // 10. WRONG ROLE
    // ============================================================

    @Test
    void deleteDonor_shouldReturn403_whenUserHasNoAdminRole()
            throws Exception {

        mockMvc.perform(
                delete("/api/donors/1")
                        .with(user("testuser")
                                .roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 11. ADMIN CAN ACCESS DELETE DONOR
    // ============================================================

    @Test
    void deleteDonor_shouldNotReturn403_whenUserIsAdmin()
            throws Exception {

        mockMvc.perform(
                delete("/api/donors/999999")
                        .with(user("admin")
                                .roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        403,
                        result.getResponse().getStatus()));
    }

    // ============================================================
    // 12. ADMIN-ONLY BLOOD STOCK UPDATE
    // ============================================================

    @Test
    void updateBloodStock_shouldReturn403_whenUserIsNotAdmin()
            throws Exception {

        mockMvc.perform(
                put("/api/bloodstock/1")
                        .with(user("testuser")
                                .roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 13. ADMIN-ONLY BLOOD STOCK DELETE
    // ============================================================

    @Test
    void deleteBloodStock_shouldReturn403_whenUserIsNotAdmin()
            throws Exception {

        mockMvc.perform(
                delete("/api/bloodstock/1")
                        .with(user("testuser")
                                .roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 14. ADMIN-ONLY DONATION UPDATE
    // ============================================================

    @Test
    void updateDonation_shouldReturn403_whenUserIsNotAdmin()
            throws Exception {

        mockMvc.perform(
                put("/api/donations/1")
                        .with(user("testuser")
                                .roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 15. ADMIN-ONLY DONATION DELETE
    // ============================================================

    @Test
    void deleteDonation_shouldReturn403_whenUserIsNotAdmin()
            throws Exception {

        mockMvc.perform(
                delete("/api/donations/1")
                        .with(user("testuser")
                                .roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}