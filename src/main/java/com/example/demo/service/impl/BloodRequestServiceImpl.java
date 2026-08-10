package com.example.demo.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.BloodRequestRequest;
import com.example.demo.dto.response.BloodRequestResponse;
import com.example.demo.entity.BloodRequest;
import com.example.demo.entity.BloodStock;
import com.example.demo.entity.User;
import com.example.demo.enums.RequestStatus;
import com.example.demo.mapper.BloodRequestMapper;
import com.example.demo.repository.BloodRequestRepository;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.BloodRequestService;

import org.springframework.transaction.annotation.Transactional;

@Service
public class BloodRequestServiceImpl implements BloodRequestService {

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public void createRequest(String email, BloodRequestRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BloodRequest bloodRequest = BloodRequestMapper.toEntity(request, user);
        bloodRequest.setRequestDate(LocalDateTime.now());
        bloodRequest.setStatus(RequestStatus.PENDING);

        bloodRequestRepository.save(bloodRequest);
    }

    @Override
    public void saveRequest(BloodRequestRequest request) {

        User user = null;
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user = userRepository.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);
        }
        if (user == null) {
            user = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No users exist in the system to submit a blood request."));
        }

        BloodRequest bloodRequest = BloodRequestMapper.toEntity(request, user);
        bloodRequest.setRequestDate(LocalDateTime.now());
        bloodRequest.setStatus(RequestStatus.PENDING);

        bloodRequestRepository.save(bloodRequest);
    }

    @Override
    public List<BloodRequestResponse> getMyRequests(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return bloodRequestRepository
                .findByUserAndDeletedFalse(user)
                .stream()
                .map(BloodRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BloodRequestResponse> getRequestsByStatus(RequestStatus status) {

        return bloodRequestRepository
                .findByStatusAndDeletedFalse(status)
                .stream()
                .map(BloodRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BloodRequestResponse> getAllRequests() {

        return bloodRequestRepository
                .findAllByDeletedFalse()
                .stream()
                .map(BloodRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BloodRequestResponse> getBloodRequests(int page, int size, String keyword, String sortBy, String direction) {
        String validSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(validSortBy).descending()
                : Sort.by(validSortBy).ascending();

        Pageable pageable = PageRequest.of(page < 0 ? 0 : page, size <= 0 ? 10 : size, sort);

        Page<BloodRequest> requestPage;
        if (keyword == null || keyword.isBlank()) {
            requestPage = bloodRequestRepository.findAllByDeletedFalse(pageable);
        } else {
            String trimmed = keyword.trim();
            requestPage = bloodRequestRepository.findByUser_FullNameContainingIgnoreCaseAndDeletedFalse(trimmed, pageable);
        }

        return requestPage.map(BloodRequestMapper::toResponse);
    }

    @Override
    public BloodRequestResponse getRequestById(Long id) {

        BloodRequest request = bloodRequestRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        return BloodRequestMapper.toResponse(request);
    }

    @Transactional
    @Override
    public void approveRequest(Long id) {

        BloodRequest request = bloodRequestRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() == RequestStatus.APPROVED) {
            throw new RuntimeException("Request is already approved.");
        }

        BloodStock stock = bloodStockRepository
                .findByBloodGroup(request.getBloodGroup())
                .orElseGet(() -> {
                    BloodStock newStock = new BloodStock();
                    newStock.setBloodGroup(request.getBloodGroup());
                    newStock.setUnitsAvailable(0);
                    newStock.setLastUpdated(LocalDateTime.now());
                    return bloodStockRepository.save(newStock);
                });

        int available = stock.getUnitsAvailable() != null ? stock.getUnitsAvailable() : 0;
        int required = request.getUnitsRequired() != null ? request.getUnitsRequired() : 0;

        if (available < required) {
            throw new RuntimeException("Cannot approve request: Insufficient blood stock for "
                    + request.getBloodGroup().getValue() + " (Available: " + available + " units, Required: " + required + " units). Please add stock in Manage Blood Stock first.");
        }

        stock.setUnitsAvailable(available - required);
        stock.setLastUpdated(LocalDateTime.now());
        bloodStockRepository.save(stock);

        request.setStatus(RequestStatus.APPROVED);
        bloodRequestRepository.save(request);

        auditLogService.saveLog(
                "Admin",
                "APPROVE",
                "Blood Request",
                "Approved Request ID : " + id);
    }

    @Transactional
    @Override
    public void rejectRequest(Long id) {

        BloodRequest request = bloodRequestRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() == RequestStatus.REJECTED) {
            throw new RuntimeException("Request is already rejected.");
        }

        request.setStatus(RequestStatus.REJECTED);
        bloodRequestRepository.save(request);

        auditLogService.saveLog(
                "Admin",
                "REJECT",
                "Blood Request",
                "Rejected Request ID : " + id);
    }
}