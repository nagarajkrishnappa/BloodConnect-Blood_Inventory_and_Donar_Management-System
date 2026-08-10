package com.example.demo.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.DonationHistoryRequest;
import com.example.demo.dto.response.DonationHistoryResponse;
import com.example.demo.entity.BloodStock;
import com.example.demo.entity.DonationHistory;
import com.example.demo.entity.Donor;
import com.example.demo.mapper.DonationHistoryMapper;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.repository.DonationHistoryRepository;
import com.example.demo.repository.DonorRepository;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.DonationHistoryService;

@Service
@Transactional
public class DonationHistoryServiceImpl implements DonationHistoryService {

    @Autowired
    private DonationHistoryRepository donationHistoryRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public void recordDonation(DonationHistoryRequest request) {

        // Find donor
        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Donor not found"));

        // Create Donation History
        DonationHistory history = DonationHistoryMapper.toEntity(request, donor);

        donationHistoryRepository.save(history);

        // Update donor details
        if (request.getDonationDate() != null) {
            donor.setLastDonationDate(request.getDonationDate());
        }
        donorRepository.save(donor);

        // Increase Blood Stock
        if (donor.getBloodGroup() != null) {
            BloodStock stock = bloodStockRepository
                    .findByBloodGroup(donor.getBloodGroup())
                    .orElseGet(() -> {
                        BloodStock newStock = new BloodStock();
                        newStock.setBloodGroup(donor.getBloodGroup());
                        newStock.setUnitsAvailable(0);
                        newStock.setLastUpdated(java.time.LocalDateTime.now());
                        return bloodStockRepository.save(newStock);
                    });

            int currentUnits = stock.getUnitsAvailable() != null ? stock.getUnitsAvailable() : 0;
            int addedUnits = request.getUnitsDonated() != null ? request.getUnitsDonated() : 0;

            stock.setUnitsAvailable(currentUnits + addedUnits);
            stock.setLastUpdated(java.time.LocalDateTime.now());

            bloodStockRepository.save(stock);
        }

        // Record Audit Log
        auditLogService.saveLog(
                "Admin",
                "ADD",
                "Donation",
                "Donation recorded successfully.");

    }

    @Override
    public void saveDonation(DonationHistoryRequest request) {
        recordDonation(request);
    }

    @Override
    public DonationHistoryResponse getDonationById(Long id) {
        DonationHistory history = donationHistoryRepository.findById(id)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Donation history record not found with ID: " + id));
        return DonationHistoryMapper.toResponse(history);
    }

    @Override
    public void updateDonation(Long id, DonationHistoryRequest request) {
        DonationHistory history = donationHistoryRepository.findById(id)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Donation history record not found with ID: " + id));

        if (request.getDonationDate() != null) {
            history.setDonationDate(request.getDonationDate());
        }
        if (request.getUnitsDonated() != null) {
            history.setUnitsDonated(request.getUnitsDonated());
        }
        if (request.getRemarks() != null) {
            history.setRemarks(request.getRemarks());
        }

        donationHistoryRepository.save(history);
    }

    @Override
    public void deleteDonation(Long id) {
        DonationHistory history = donationHistoryRepository.findById(id)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Donation history record not found with ID: " + id));

        history.setDeleted(true);
        donationHistoryRepository.save(history);
    }

    @Override
    public List<DonationHistoryResponse> getAllDonations() {

        return donationHistoryRepository
                .findAllByDeletedFalse()
                .stream()
                .map(DonationHistoryMapper::toResponse)
                .collect(Collectors.toList());

    }

    @Override
    public org.springframework.data.domain.Page<DonationHistoryResponse> getDonations(int page, int size, String keyword, String sortBy, String direction) {
        String validSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        org.springframework.data.domain.Sort sort = "desc".equalsIgnoreCase(direction)
                ? org.springframework.data.domain.Sort.by(validSortBy).descending()
                : org.springframework.data.domain.Sort.by(validSortBy).ascending();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page < 0 ? 0 : page, size <= 0 ? 10 : size, sort);

        org.springframework.data.domain.Page<DonationHistory> donationPage;
        if (keyword == null || keyword.isBlank()) {
            donationPage = donationHistoryRepository.findAllByDeletedFalse(pageable);
        } else {
            String trimmed = keyword.trim();
            donationPage = donationHistoryRepository.findByDonor_User_FullNameContainingIgnoreCaseAndDeletedFalse(trimmed, pageable);
        }

        return donationPage.map(DonationHistoryMapper::toResponse);
    }

}