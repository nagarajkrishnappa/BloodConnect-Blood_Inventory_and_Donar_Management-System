package com.example.demo.mapper;

import java.time.LocalDateTime;

import com.example.demo.dto.request.BloodRequestRequest;
import com.example.demo.dto.response.BloodRequestResponse;
import com.example.demo.entity.BloodRequest;
import com.example.demo.entity.User;
import com.example.demo.enums.RequestStatus;

public class BloodRequestMapper {

    private BloodRequestMapper() {
    }

    public static BloodRequest toEntity(BloodRequestRequest request, User user) {

        BloodRequest bloodRequest = new BloodRequest();

        bloodRequest.setUser(user);
        bloodRequest.setBloodGroup(request.getBloodGroup());
        bloodRequest.setUnitsRequired(request.getUnitsRequired());
        bloodRequest.setReason(request.getReason());
        bloodRequest.setStatus(RequestStatus.PENDING);
        bloodRequest.setRequestDate(LocalDateTime.now());
        bloodRequest.setDeleted(false);

        return bloodRequest;
    }

    public static BloodRequestResponse toResponse(BloodRequest request) {

        BloodRequestResponse response = new BloodRequestResponse();

        response.setId(request.getId());
        if (request.getUser() != null) {
            response.setFullName(request.getUser().getFullName());
            response.setEmail(request.getUser().getEmail());
        }
        response.setBloodGroup(request.getBloodGroup());
        response.setUnitsRequired(request.getUnitsRequired());
        response.setReason(request.getReason());
        response.setStatus(request.getStatus());
        response.setRequestDate(request.getRequestDate());

        return response;
    }
}
