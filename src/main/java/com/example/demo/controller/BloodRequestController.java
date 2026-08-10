package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.request.BloodRequestRequest;
import com.example.demo.dto.response.BloodRequestResponse;
import com.example.demo.enums.BloodGroup;
import com.example.demo.service.BloodRequestService;

@Controller
public class BloodRequestController {

    @Autowired
    private BloodRequestService bloodRequestService;

    // -----------------------------
    // User Request Form
    // -----------------------------
    @GetMapping("/user/request-blood")
    public String requestBloodForm(Model model) {

        model.addAttribute("bloodRequest", new BloodRequestRequest());
        model.addAttribute("bloodGroups", BloodGroup.values());

        return "user/request-blood";
    }

    // -----------------------------
    // Save Request
    // -----------------------------
    @PostMapping("/user/request-blood")
    public String saveRequest(
            @ModelAttribute BloodRequestRequest request,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            bloodRequestService.createRequest(
                    principal.getName(),
                    request);

            redirectAttributes.addFlashAttribute("successMessage", "Blood request submitted successfully!");
            return "redirect:/user/my-requests";

        } catch (Exception e) {

            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("bloodGroups", BloodGroup.values());

            return "user/request-blood";
        }
    }

    // -----------------------------
    // My Requests
    // -----------------------------
    @GetMapping("/user/my-requests")
    public String myRequests(
            Principal principal,
            Model model) {

        model.addAttribute(
                "requests",
                bloodRequestService.getMyRequests(principal.getName()));

        return "user/my-requests";
    }

    // -----------------------------
    // Admin View with Pagination
    // -----------------------------
    @GetMapping({"/admin/bloodrequests", "/admin/blood-requests"})
    public String allRequests(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            Model model) {

        Page<BloodRequestResponse> requestPage = bloodRequestService.getBloodRequests(page, size, keyword, sortBy, direction);

        model.addAttribute("requestPage", requestPage);
        model.addAttribute("requests", requestPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", requestPage.getTotalPages());
        model.addAttribute("totalItems", requestPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "admin/bloodrequest/list";
    }

    // -----------------------------
    // Approve
    // -----------------------------
    @GetMapping("/admin/bloodrequests/approve/{id}")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            bloodRequestService.approveRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Blood request approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/bloodrequests";
    }

    // -----------------------------
    // Reject
    // -----------------------------
    @GetMapping("/admin/bloodrequests/reject/{id}")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            bloodRequestService.rejectRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Blood request rejected successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/bloodrequests";
    }

}
