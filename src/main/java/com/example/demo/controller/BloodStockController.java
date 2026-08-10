package com.example.demo.controller;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.request.BloodStockRequest;
import com.example.demo.dto.response.BloodStockResponse;
import com.example.demo.enums.BloodGroup;
import com.example.demo.service.BloodStockService;
import com.example.demo.util.excel.BloodStockExcelGenerator;
import com.example.demo.util.pdf.BloodStockPdfGenerator;

import jakarta.validation.Valid;

@Controller
public class BloodStockController {

    @Autowired
    private BloodStockService bloodStockService;

    // ------------------------
    // Show Add Blood Form
    // ------------------------
    @GetMapping("/admin/bloodstock/add")
    public String showAddForm(Model model) {
        model.addAttribute("bloodStockRequest", new BloodStockRequest());
        model.addAttribute("bloodGroups", BloodGroup.values());
        return "admin/bloodstock/add";
    }

    // ------------------------
    // Save Blood Stock
    // ------------------------
    @PostMapping("/admin/bloodstock/save")
    public String saveBloodStock(
            @Valid @ModelAttribute("bloodStockRequest") BloodStockRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldError() != null 
                    ? bindingResult.getFieldError().getDefaultMessage() 
                    : "Validation error occurred.";
            model.addAttribute("errorMessage", firstError);
            model.addAttribute("bloodGroups", BloodGroup.values());
            return "admin/bloodstock/add";
        }

        try {
            bloodStockService.saveBloodStock(request);
            return "redirect:/admin/bloodstock/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("bloodGroups", BloodGroup.values());
            return "admin/bloodstock/add";
        }
    }

    // ------------------------
    // View & Paginate Blood Stock
    // ------------------------
    @GetMapping({"/admin/bloodstock/list", "/admin/bloodstock/search"})
    public String listBloodStock(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "bloodGroup", required = false) String bloodGroup,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            Model model) {

        String searchTerm = (keyword != null && !keyword.isBlank()) ? keyword : bloodGroup;
        Page<BloodStockResponse> stockPage = bloodStockService.getBloodStocks(page, size, searchTerm, sortBy, direction);

        model.addAttribute("stockPage", stockPage);
        model.addAttribute("bloodStocks", stockPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", stockPage.getTotalPages());
        model.addAttribute("totalItems", stockPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("keyword", searchTerm != null ? searchTerm : "");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "admin/bloodstock/list";
    }

    // ------------------------
    // Edit Blood Stock
    // ------------------------
    @GetMapping("/admin/bloodstock/edit/{id}")
    public String editBloodStock(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("bloodStockRequest", bloodStockService.getBloodStockById(id));
        model.addAttribute("bloodGroups", BloodGroup.values());
        return "admin/bloodstock/edit";
    }

    // ------------------------
    // Update Blood Stock
    // ------------------------
    @PostMapping("/admin/bloodstock/update/{id}")
    public String updateBloodStock(
            @PathVariable Long id,
            @Valid @ModelAttribute("bloodStockRequest") BloodStockRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldError() != null 
                    ? bindingResult.getFieldError().getDefaultMessage() 
                    : "Validation error occurred.";
            model.addAttribute("id", id);
            model.addAttribute("errorMessage", firstError);
            model.addAttribute("bloodGroups", BloodGroup.values());
            return "admin/bloodstock/edit";
        }

        try {
            bloodStockService.updateBloodStock(id, request);
            return "redirect:/admin/bloodstock/list";
        } catch (Exception e) {
            model.addAttribute("id", id);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("bloodGroups", BloodGroup.values());
            return "admin/bloodstock/edit";
        }
    }

    // ------------------------
    // Delete
    // ------------------------
    @GetMapping("/admin/bloodstock/delete/{id}")
    public String deleteBloodStock(@PathVariable Long id) {
        bloodStockService.deleteBloodStock(id);
        return "redirect:/admin/bloodstock/list";
    }

    // ------------------------
    // User Blood Availability
    // ------------------------
    @GetMapping("/user/bloodstock")
    public String availableBlood(Model model) {
        model.addAttribute("bloodStocks", bloodStockService.getAvailableBlood());
        return "user/bloodstock";
    }

    // ------------------------
    // Download PDF Report
    // ------------------------
    @GetMapping("/admin/bloodstock/pdf")
    public ResponseEntity<InputStreamResource> downloadPdf() {
        ByteArrayInputStream pdf = BloodStockPdfGenerator.generate(bloodStockService.getAllBloodStock());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=blood-stock-report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }

    @GetMapping("/admin/bloodstock/excel")
    public ResponseEntity<InputStreamResource> downloadExcel() {
        ByteArrayInputStream excel = BloodStockExcelGenerator.generate(bloodStockService.getAllBloodStock());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=blood-stock-report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excel));
    }

}