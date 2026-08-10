package com.example.demo.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.dto.response.DonorResponse;
import com.example.demo.service.BloodRequestService;
import com.example.demo.service.BloodStockService;
import com.example.demo.service.DonorService;

@Controller
public class UserController {

    @Autowired
    private DonorService donorService;

    @Autowired
    private BloodStockService bloodStockService;

    @Autowired
    private BloodRequestService bloodRequestService;

    @GetMapping("/user/dashboard")
    public String userDashboard(Principal principal, Model model) {
        if (principal != null) {
            try {
                DonorResponse donor = donorService.getDonorProfile(principal.getName());
                model.addAttribute("isDonor", true);
                model.addAttribute("donor", donor);
            } catch (Exception e) {
                model.addAttribute("isDonor", false);
            }

            try {
                int totalUnits = bloodStockService.getAvailableBlood()
                        .stream()
                        .mapToInt(b -> b.getUnits() != null ? b.getUnits() : 0)
                        .sum();
                model.addAttribute("totalAvailableUnits", totalUnits);
            } catch (Exception e) {
                model.addAttribute("totalAvailableUnits", 0);
            }

            try {
                int myRequestsCount = bloodRequestService.getMyRequests(principal.getName()).size();
                model.addAttribute("myRequestsCount", myRequestsCount);
            } catch (Exception e) {
                model.addAttribute("myRequestsCount", 0);
            }
        }
        return "user/dashboard";
    }

}