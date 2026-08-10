package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.demo.dto.request.DonorRequest;
import com.example.demo.dto.response.DonorResponse;

public interface DonorService {

    void registerDonor(String email, DonorRequest request);

    DonorResponse getDonorProfile(String email);

    DonorResponse getDonorById(Long id);

    void saveDonor(DonorRequest request);

    DonorResponse updateDonor(String email, DonorRequest request);

    void updateDonorById(Long id, DonorRequest request);

    DonorRequest getDonorForEdit(String email);

    void deleteDonor(String email);

    void deleteDonorById(Long id);

    List<DonorResponse> getAllDonors();

    Page<DonorResponse> getDonors(int page, int size, String keyword, String sortBy, String direction);

    List<DonorResponse> searchByBloodGroup(String bloodGroup);

    List<DonorResponse> searchByCity(String city);

    List<DonorResponse> searchByBloodGroupAndCity(String bloodGroup, String city);

    List<DonorResponse> searchDonors(String bloodGroupStr, String city);

    List<DonorResponse> getAvailableDonors();

}
