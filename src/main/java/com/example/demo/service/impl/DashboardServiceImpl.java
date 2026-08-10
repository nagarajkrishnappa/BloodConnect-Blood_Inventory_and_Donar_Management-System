package com.example.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.response.DashboardResponse;
import com.example.demo.entity.BloodStock;
import com.example.demo.enums.RequestStatus;
import com.example.demo.repository.BloodRequestRepository;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.repository.DonationHistoryRepository;
import com.example.demo.repository.DonorRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private DonationHistoryRepository donationHistoryRepository;

    @Override
    public DashboardResponse getDashboard() {

        DashboardResponse response = new DashboardResponse();

        response.setTotalUsers(userRepository.count());

        response.setTotalDonors(
                donorRepository.countByDeletedFalse());

        List<BloodStock> stocks = bloodStockRepository.findAll();

        long totalUnits = stocks.stream()
                .mapToLong(BloodStock::getUnitsAvailable)
                .sum();

        response.setTotalBloodUnits(totalUnits);

        response.setTotalBloodRequests(
                bloodRequestRepository.countByDeletedFalse());

        response.setApprovedRequests(
                bloodRequestRepository.countByStatusAndDeletedFalse(
                        RequestStatus.APPROVED));

        response.setPendingRequests(
                bloodRequestRepository.countByStatusAndDeletedFalse(
                        RequestStatus.PENDING));

        response.setRejectedRequests(
                bloodRequestRepository.countByStatusAndDeletedFalse(
                        RequestStatus.REJECTED));

        response.setTotalDonations(
                donationHistoryRepository.countByDeletedFalse());

        return response;
    }

}