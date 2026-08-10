package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.demo.dto.request.DonationHistoryRequest;
import com.example.demo.dto.response.DonationHistoryResponse;

public interface DonationHistoryService {

    void recordDonation(DonationHistoryRequest request);

    void saveDonation(DonationHistoryRequest request);

    DonationHistoryResponse getDonationById(Long id);

    void updateDonation(Long id, DonationHistoryRequest request);

    void deleteDonation(Long id);

    List<DonationHistoryResponse> getAllDonations();

    Page<DonationHistoryResponse> getDonations(int page, int size, String keyword, String sortBy, String direction);

}