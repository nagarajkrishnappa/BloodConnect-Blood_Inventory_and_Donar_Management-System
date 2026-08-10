package com.example.demo.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.request.DonationHistoryRequest;
import com.example.demo.dto.response.DonationHistoryResponse;
import com.example.demo.service.DonationHistoryService;

@RestController
@RequestMapping("/api/donations")
public class DonationHistoryApiController {

    @Autowired
    private DonationHistoryService donationHistoryService;

    // ======================================
    // Get All Donations
    // ======================================

    @GetMapping
    public ResponseEntity<List<DonationHistoryResponse>> getAllDonations() {

        return ResponseEntity.ok(
                donationHistoryService.getAllDonations());
    }

    // ======================================
    // Get Donation By Id
    // ======================================

    @GetMapping("/{id}")
    public ResponseEntity<DonationHistoryResponse> getDonationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                donationHistoryService.getDonationById(id));
    }

    // ======================================
    // Add Donation
    // ======================================

    @PostMapping
    public ResponseEntity<String> addDonation(
            @RequestBody DonationHistoryRequest request) {

        donationHistoryService.saveDonation(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Donation Recorded Successfully");
    }

    // ======================================
    // Update Donation
    // ======================================

    @PutMapping("/{id}")
    public ResponseEntity<String> updateDonation(
            @PathVariable Long id,
            @RequestBody DonationHistoryRequest request) {

        donationHistoryService.updateDonation(id, request);

        return ResponseEntity.ok(
                "Donation Updated Successfully");
    }

    // ======================================
    // Delete Donation
    // ======================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDonation(
            @PathVariable Long id) {

        donationHistoryService.deleteDonation(id);

        return ResponseEntity.ok(
                "Donation Deleted Successfully");
    }

}