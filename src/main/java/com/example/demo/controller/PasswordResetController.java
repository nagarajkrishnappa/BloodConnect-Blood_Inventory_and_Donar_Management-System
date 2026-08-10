package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.request.ForgotPasswordRequest;
import com.example.demo.dto.request.ResetPasswordRequest;
import com.example.demo.service.PasswordResetService;

import jakarta.validation.Valid;

@Controller
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        if (!model.containsAttribute("forgotPasswordRequest")) {
            model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        }
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldError() != null 
                    ? bindingResult.getFieldError().getDefaultMessage() 
                    : "Validation error occurred.";
            model.addAttribute("errorMessage", firstError);
            return "forgot-password";
        }

        try {
            passwordResetService.requestPasswordReset(request.getEmail());
            model.addAttribute("successMessage", "Password reset link has been sent to your email.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("forgotPasswordRequest", request);
        }

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(name = "token", required = false) String token,
            Model model) {

        if (token == null || token.isBlank()) {
            model.addAttribute("errorMessage", "Password reset token is missing.");
        }

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);

        model.addAttribute("resetPasswordRequest", request);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldError() != null 
                    ? bindingResult.getFieldError().getDefaultMessage() 
                    : "Validation error occurred.";
            model.addAttribute("errorMessage", firstError);
            return "reset-password";
        }

        try {
            passwordResetService.resetPassword(
                    request.getToken(),
                    request.getNewPassword(),
                    request.getConfirmPassword());

            redirectAttributes.addFlashAttribute("successMessage", "Password reset successful! Please login with your new password.");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("resetPasswordRequest", request);
            return "reset-password";
        }
    }

}