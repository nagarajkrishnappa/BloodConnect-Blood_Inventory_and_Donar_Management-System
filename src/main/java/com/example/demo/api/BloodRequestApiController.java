package com.example.demo.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.request.BloodRequestRequest;
import com.example.demo.dto.response.BloodRequestResponse;
import com.example.demo.service.BloodRequestService;

@RestController
@RequestMapping("/api/bloodrequests")
public class BloodRequestApiController {

    @Autowired
    private BloodRequestService bloodRequestService;

    // ================================
    // Get All Requests
    // ================================

    @GetMapping
    public ResponseEntity<List<BloodRequestResponse>> getAllRequests() {

        return ResponseEntity.ok(
                bloodRequestService.getAllRequests());

    }

    // ================================
    // Get My Requests
    // ================================

    @GetMapping("/my")
    public ResponseEntity<List<BloodRequestResponse>> getMyRequests(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "";

        return ResponseEntity.ok(
                bloodRequestService.getMyRequests(email));

    }

    // ================================
    // Get Requests By Status
    // ================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BloodRequestResponse>> getRequestsByStatus(
            @PathVariable com.example.demo.enums.RequestStatus status) {

        return ResponseEntity.ok(
                bloodRequestService.getRequestsByStatus(status));

    }

    // ================================
    // Get Request By ID
    // ================================

    @GetMapping("/{id}")
    public ResponseEntity<BloodRequestResponse> getRequestById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                bloodRequestService.getRequestById(id));

    }

    // ================================
    // Create Blood Request
    // ================================

    @PostMapping
    public ResponseEntity<String> createRequest(
            @RequestBody BloodRequestRequest request) {

        bloodRequestService.saveRequest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Blood Request Submitted Successfully");

    }

    // ================================
    // Approve Request
    // ================================

    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveRequest(
            @PathVariable Long id) {

        bloodRequestService.approveRequest(id);

        return ResponseEntity.ok(
                "Blood Request Approved Successfully");

    }

    // ================================
    // Reject Request
    // ================================

    @PutMapping("/{id}/reject")
    public ResponseEntity<String> rejectRequest(
            @PathVariable Long id) {

        bloodRequestService.rejectRequest(id);

        return ResponseEntity.ok(
                "Blood Request Rejected Successfully");

    }

}