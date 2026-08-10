package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.response.ChartDataResponse;
import com.example.demo.dto.response.DashboardResponse;
import com.example.demo.service.DashboardAnalyticsService;
import com.example.demo.service.DashboardService;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.response.ChartDataResponse;
import com.example.demo.service.DashboardAnalyticsService;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DashboardAnalyticsService dashboardAnalyticsService;

    // -------------------------
    // Admin Dashboard
    // -------------------------
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {

        DashboardResponse dashboard = dashboardService.getDashboard();

        model.addAttribute("dashboard", dashboard);

        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard/chart/bloodstock")
    @ResponseBody
    public ChartDataResponse bloodStockChart() {

        return dashboardAnalyticsService.getBloodStockChart();

    }

    @GetMapping("/admin/dashboard/chart/requests")
    @ResponseBody
    public ChartDataResponse requestChart() {

        return dashboardAnalyticsService.getRequestStatusChart();

    }

    @GetMapping("/admin/dashboard/chart/donations")
    @ResponseBody
    public ChartDataResponse donationChart() {

        return dashboardAnalyticsService.getDonationChart();

    }
}