package com.example.demo.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.request.BloodStockRequest;
import com.example.demo.entity.BloodStock;
import com.example.demo.enums.BloodGroup;
import com.example.demo.repository.BloodStockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class BloodStockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BloodStockRepository bloodStockRepository;

    private BloodStock testStock;

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        bloodStockRepository.deleteAll();

        testStock = new BloodStock();

        testStock.setBloodGroup(BloodGroup.O_POSITIVE);
        testStock.setUnitsAvailable(10);
        testStock.setLastUpdated(LocalDateTime.now());

        testStock = bloodStockRepository.save(testStock);
    }

    // =========================================================
    // GET ALL BLOOD STOCK
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void getAllBloodStock_shouldReturn200_forUser()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodstock"))
                .andExpect(status().isOk());
    }

    // =========================================================
    // GET ALL BLOOD STOCK - ADMIN
    // =========================================================

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void getAllBloodStock_shouldReturn200_forAdmin()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodstock"))
                .andExpect(status().isOk());
    }

    // =========================================================
    // GET BLOOD STOCK BY ID
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void getBloodStockById_shouldReturn200_whenExists()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodstock/{id}",
                        testStock.getId()))
                .andExpect(status().isOk());
    }

    // =========================================================
    // GET BLOOD STOCK BY INVALID ID
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void getBloodStockById_shouldReturn404_whenNotFound()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodstock/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // ADD BLOOD STOCK - ADMIN
    // =========================================================

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void addBloodStock_shouldReturn201_forAdmin()
            throws Exception {

        BloodStockRequest request = new BloodStockRequest();

        request.setBloodGroup(BloodGroup.A_POSITIVE);
        request.setUnits(20);

        mockMvc.perform(
                post("/api/bloodstock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // =========================================================
    // ADD DUPLICATE BLOOD GROUP
    // =========================================================

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void addBloodStock_shouldReturnConflict_whenBloodGroupExists()
            throws Exception {

        BloodStockRequest request = new BloodStockRequest();

        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setUnits(20);

        mockMvc.perform(
                post("/api/bloodstock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // =========================================================
    // UPDATE BLOOD STOCK - ADMIN
    // =========================================================

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void updateBloodStock_shouldReturn200_forAdmin()
            throws Exception {

        BloodStockRequest request = new BloodStockRequest();

        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setUnits(25);

        mockMvc.perform(
                put("/api/bloodstock/{id}",
                        testStock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // =========================================================
    // UPDATE NON EXISTING BLOOD STOCK
    // =========================================================

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void updateBloodStock_shouldReturn404_whenNotFound()
            throws Exception {

        BloodStockRequest request = new BloodStockRequest();

        request.setBloodGroup(BloodGroup.A_POSITIVE);
        request.setUnits(25);

        mockMvc.perform(
                put("/api/bloodstock/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // DELETE BLOOD STOCK - ADMIN
    // =========================================================

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void deleteBloodStock_shouldReturn200_forAdmin()
            throws Exception {

        mockMvc.perform(
                delete("/api/bloodstock/{id}",
                        testStock.getId()))
                .andExpect(status().isOk());
    }

    // =========================================================
    // USER CANNOT ADD BLOOD STOCK
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void addBloodStock_shouldReturn403_forUser()
            throws Exception {

        BloodStockRequest request = new BloodStockRequest();

        request.setBloodGroup(BloodGroup.A_POSITIVE);
        request.setUnits(10);

        mockMvc.perform(
                post("/api/bloodstock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // USER CANNOT UPDATE BLOOD STOCK
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void updateBloodStock_shouldReturn403_forUser()
            throws Exception {

        BloodStockRequest request = new BloodStockRequest();

        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setUnits(30);

        mockMvc.perform(
                put("/api/bloodstock/{id}",
                        testStock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // USER CANNOT DELETE BLOOD STOCK
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void deleteBloodStock_shouldReturn403_forUser()
            throws Exception {

        mockMvc.perform(
                delete("/api/bloodstock/{id}",
                        testStock.getId()))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // UNAUTHENTICATED USER
    // =========================================================

    @Test
    void getBloodStock_shouldReturn401_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/bloodstock"))
                .andExpect(status().isUnauthorized());
    }
}
