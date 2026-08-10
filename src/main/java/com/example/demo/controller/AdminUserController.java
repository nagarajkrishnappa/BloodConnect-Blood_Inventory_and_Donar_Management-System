package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AdminUserController {

    @Autowired
    private UserService userService;

    // View, search, sort & paginate users
    @GetMapping({"/admin/users", "/admin/manage-users", "/admin/users/search"})
    public String viewUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "sortBy", defaultValue = "fullName") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            Model model) {

        Page<UserResponse> usersPage = userService.getUsers(page, size, keyword, sortBy, direction);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalItems", usersPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "admin/user/list";
    }

    // Show edit user form
    @GetMapping("/admin/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UserResponse user = userService.getUserById(id);

            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setId(user.getId());
            updateRequest.setFullName(user.getFullName());
            updateRequest.setEmail(user.getEmail());
            updateRequest.setPhone(user.getPhone());
            updateRequest.setRole(user.getRole());
            updateRequest.setEnabled(user.getEnabled());

            model.addAttribute("userRequest", updateRequest);
            return "admin/user/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    // Save updated user profile & role
    @PostMapping("/admin/users/update/{id}")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute("userRequest") UserUpdateRequest updateRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldError() != null 
                    ? bindingResult.getFieldError().getDefaultMessage() 
                    : "Validation failed.";
            model.addAttribute("errorMessage", firstError);
            return "admin/user/edit";
        }

        try {
            userService.updateUser(id, updateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userRequest", updateRequest);
            return "admin/user/edit";
        }
    }

    // Enable user
    @GetMapping("/admin/users/enable/{id}")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            userService.enableUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account enabled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // Disable user
    @GetMapping("/admin/users/disable/{id}")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            userService.disableUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account disabled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // Delete user
    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

}