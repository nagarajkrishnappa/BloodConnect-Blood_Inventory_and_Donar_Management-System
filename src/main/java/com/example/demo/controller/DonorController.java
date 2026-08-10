package com.example.demo.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.request.DonorRequest;
import com.example.demo.dto.response.DonorResponse;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.Gender;
import com.example.demo.service.DonorService;

@Controller
public class DonorController {

    @Autowired
    private DonorService donorService;

    @GetMapping({ "/user/donor/register", "/donor/register" })
    public String showDonorForm(Principal principal, Model model) {
        if (principal != null) {
            try {
                donorService.getDonorProfile(principal.getName());
                return "redirect:/user/donor/profile";
            } catch (Exception ignored) {
            }
        }

        model.addAttribute("donorRequest", new DonorRequest());
        model.addAttribute("bloodGroups", BloodGroup.values());
        model.addAttribute("genders", Gender.values());

        return "donor/register";
    }

    @PostMapping({ "/user/donor/register", "/donor/register" })
    public String registerDonor(@ModelAttribute DonorRequest request,
            Principal principal,
            Model model) {

        try {
            donorService.registerDonor(principal.getName(), request);
            return "redirect:/user/donor/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("donorRequest", request);
            model.addAttribute("bloodGroups", BloodGroup.values());
            model.addAttribute("genders", Gender.values());

            return "donor/register";
        }
    }

    @GetMapping({ "/user/donor/profile", "/donor/profile" })
    public String donorProfile(Principal principal, Model model) {
        try {
            model.addAttribute("donor", donorService.getDonorProfile(principal.getName()));
            return "donor/profile";
        } catch (Exception e) {
            return "redirect:/user/donor/register";
        }
    }

    @GetMapping({ "/user/donor/edit", "/donor/edit" })
    public String editDonor(Principal principal, Model model) {
        try {
            model.addAttribute("donorRequest", donorService.getDonorForEdit(principal.getName()));
            model.addAttribute("bloodGroups", BloodGroup.values());
            model.addAttribute("genders", Gender.values());

            return "donor/edit";
        } catch (Exception e) {
            return "redirect:/user/donor/register";
        }
    }

    @PostMapping({ "/user/donor/edit", "/donor/edit" })
    public String updateDonor(@ModelAttribute DonorRequest request,
            Principal principal,
            Model model) {
        try {
            donorService.updateDonor(principal.getName(), request);
            return "redirect:/user/donor/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("donorRequest", request);
            model.addAttribute("bloodGroups", BloodGroup.values());
            model.addAttribute("genders", Gender.values());
            return "donor/edit";
        }
    }

    @GetMapping({ "/user/donor/delete", "/donor/delete" })
    public String deleteDonor(Principal principal) {
        try {
            donorService.deleteDonor(principal.getName());
        } catch (Exception ignored) {
        }
        return "redirect:/user/dashboard";
    }

    @GetMapping({ "/admin/donors", "/user/donors", "/donors", "/user/donors/search", "/donors/search" })
    public String viewDonors(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            Model model) {

        Page<DonorResponse> donorPage = donorService.getDonors(page, size, keyword, sortBy, direction);

        model.addAttribute("donorPage", donorPage);
        model.addAttribute("donors", donorPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", donorPage.getTotalPages());
        model.addAttribute("totalItems", donorPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("bloodGroups", BloodGroup.values());

        return "donor/list";
    }

}
