package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.request.DonationHistoryRequest;
import com.example.demo.dto.response.DonationHistoryResponse;
import com.example.demo.service.DonationHistoryService;
import com.example.demo.service.DonorService;

@Controller
@RequestMapping("/admin/donations")
public class DonationHistoryController {

    @Autowired
    private DonationHistoryService donationHistoryService;

    @Autowired
    private DonorService donorService;

    // Show Donation Form
    @GetMapping("/add")
    public String showDonationForm(Model model) {

        model.addAttribute("donationRequest", new DonationHistoryRequest());

        model.addAttribute(
                "donors",
                donorService.getAllDonors());

        return "admin/donation/add";
    }

    // Save Donation
    @PostMapping("/save")
    public String saveDonation(
            @ModelAttribute DonationHistoryRequest request) {

        donationHistoryService.recordDonation(request);

        return "redirect:/admin/donations/list";
    }

    // Donation History with Pagination
    @GetMapping({"", "/list", "/search"})
    public String donationHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            Model model) {

        Page<DonationHistoryResponse> donationPage = donationHistoryService.getDonations(page, size, keyword, sortBy, direction);

        model.addAttribute("donationPage", donationPage);
        model.addAttribute("donations", donationPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", donationPage.getTotalPages());
        model.addAttribute("totalItems", donationPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "admin/donation/list";
    }

}