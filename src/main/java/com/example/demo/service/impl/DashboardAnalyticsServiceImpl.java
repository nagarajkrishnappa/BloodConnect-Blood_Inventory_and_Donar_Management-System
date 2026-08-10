package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.response.ChartDataResponse;
import com.example.demo.entity.BloodStock;
import com.example.demo.enums.RequestStatus;
import com.example.demo.repository.BloodRequestRepository;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.repository.DonationHistoryRepository;
import com.example.demo.service.DashboardAnalyticsService;

@Service
public class DashboardAnalyticsServiceImpl
        implements DashboardAnalyticsService {

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private DonationHistoryRepository donationHistoryRepository;

    @Override
    public ChartDataResponse getBloodStockChart() {

        List<BloodStock> stocks = bloodStockRepository.findAllByOrderByBloodGroup();

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (BloodStock stock : stocks) {

            labels.add(stock.getBloodGroup().getValue());

            values.add((long) stock.getUnitsAvailable());

        }

        return new ChartDataResponse(labels, values);

    }

    @Override
    public ChartDataResponse getRequestStatusChart() {

        List<String> labels = List.of(
                "Pending",
                "Approved",
                "Rejected");

        List<Long> values = List.of(

                bloodRequestRepository.countByStatusAndDeletedFalse(
                        RequestStatus.PENDING),

                bloodRequestRepository.countByStatusAndDeletedFalse(
                        RequestStatus.APPROVED),

                bloodRequestRepository.countByStatusAndDeletedFalse(
                        RequestStatus.REJECTED)

        );

        return new ChartDataResponse(labels, values);

    }

    @Override
    public ChartDataResponse getDonationChart() {

        List<String> labels = List.of(
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec");

        List<Long> values = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {

            values.add(0L);

        }

        return new ChartDataResponse(labels, values);

    }

}