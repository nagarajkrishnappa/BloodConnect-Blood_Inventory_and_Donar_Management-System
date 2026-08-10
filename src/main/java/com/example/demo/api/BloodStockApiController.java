package com.example.demo.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.request.BloodStockRequest;
import com.example.demo.dto.response.BloodStockResponse;
import com.example.demo.service.BloodStockService;

@RestController
@RequestMapping("/api/bloodstock")
public class BloodStockApiController {

    @Autowired
    private BloodStockService bloodStockService;

    // =====================================
    // Get All Blood Stock
    // =====================================

    @GetMapping
    public ResponseEntity<List<BloodStockResponse>> getAllBloodStock() {

        return ResponseEntity.ok(
                bloodStockService.getAllBloodStock());
    }

    // =====================================
    // Get Blood Stock By Id
    // =====================================

    @GetMapping("/{id}")
    public ResponseEntity<BloodStockResponse> getBloodStockById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                bloodStockService.getBloodStockResponseById(id));
    }

    // =====================================
    // Add Blood Stock
    // =====================================

    @PostMapping
    public ResponseEntity<String> addBloodStock(
            @RequestBody BloodStockRequest request) {

        bloodStockService.saveBloodStock(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Blood Stock Added Successfully");
    }

    // =====================================
    // Update Blood Stock
    // =====================================

    @PutMapping("/{id}")
    public ResponseEntity<String> updateBloodStock(
            @PathVariable Long id,
            @RequestBody BloodStockRequest request) {

        bloodStockService.updateBloodStock(id, request);

        return ResponseEntity.ok(
                "Blood Stock Updated Successfully");
    }

    // =====================================
    // Delete Blood Stock
    // =====================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBloodStock(
            @PathVariable Long id) {

        bloodStockService.deleteBloodStock(id);

        return ResponseEntity.ok(
                "Blood Stock Deleted Successfully");
    }

}