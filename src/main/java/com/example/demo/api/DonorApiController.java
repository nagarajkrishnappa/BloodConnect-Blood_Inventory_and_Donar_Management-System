package com.example.demo.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.request.DonorRequest;
import com.example.demo.dto.response.DonorResponse;
import com.example.demo.service.DonorService;

@RestController
@RequestMapping("/api/donors")
public class DonorApiController {

    @Autowired
    private DonorService donorService;

    // ====================================
    // Get All Donors
    // ====================================

    @GetMapping
    public ResponseEntity<List<DonorResponse>> getAllDonors() {

        return ResponseEntity.ok(
                donorService.getAllDonors());
    }

    // ====================================
    // Get Donor By ID
    // ====================================

    @GetMapping("/{id}")
    public ResponseEntity<DonorResponse> getDonorById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                donorService.getDonorById(id));
    }

    // ====================================
    // Add Donor
    // ====================================

    @PostMapping
    public ResponseEntity<String> addDonor(
            @RequestBody DonorRequest request) {

        donorService.saveDonor(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Donor Added Successfully");
    }

    // ====================================
    // Update Donor
    // ====================================

    @PutMapping("/{id}")
    public ResponseEntity<String> updateDonor(
            @PathVariable Long id,
            @RequestBody DonorRequest request) {

        donorService.updateDonorById(id, request);

        return ResponseEntity.ok(
                "Donor Updated Successfully");
    }

    // ====================================
    // Delete Donor
    // ====================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDonor(
            @PathVariable Long id) {

        donorService.deleteDonorById(id);

        return ResponseEntity.ok(
                "Donor Deleted Successfully");
    }

}