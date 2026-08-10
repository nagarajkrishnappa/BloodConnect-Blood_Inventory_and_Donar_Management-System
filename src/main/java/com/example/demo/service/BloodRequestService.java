package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.demo.dto.request.BloodRequestRequest;
import com.example.demo.dto.response.BloodRequestResponse;

public interface BloodRequestService {

    void createRequest(String email, BloodRequestRequest request);

    void saveRequest(BloodRequestRequest request);

    List<BloodRequestResponse> getMyRequests(String email);

    List<BloodRequestResponse> getRequestsByStatus(com.example.demo.enums.RequestStatus status);

    List<BloodRequestResponse> getAllRequests();

    Page<BloodRequestResponse> getBloodRequests(int page, int size, String keyword, String sortBy, String direction);

    BloodRequestResponse getRequestById(Long id);

    void approveRequest(Long id);

    void rejectRequest(Long id);
}