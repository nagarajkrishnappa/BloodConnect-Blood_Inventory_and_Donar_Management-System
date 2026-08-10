package com.example.demo.service;

import com.example.demo.dto.response.ChartDataResponse;

public interface DashboardAnalyticsService {

    ChartDataResponse getBloodStockChart();

    ChartDataResponse getRequestStatusChart();

    ChartDataResponse getDonationChart();

}