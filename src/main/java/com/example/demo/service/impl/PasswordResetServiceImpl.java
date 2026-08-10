package com.example.demo.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.PasswordResetService;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.base-url:http://192.168.0.101:9090}")
    private String baseUrl;

    @Transactional
    @Override
    public void requestPasswordReset(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email address is required.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("No account found with this email address."));

        // Remove existing token if present
        tokenRepository.deleteByUser(user);

        // Generate secure token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));

        tokenRepository.save(resetToken);

        String resetLink = baseUrl + "/reset-password?token=" + token;

        // Print reset link directly in terminal console for dev testing
        System.out.println("=========================================================================");
        System.out.println("PASSWORD RESET LINK GENERATED FOR: " + user.getEmail());
        System.out.println("CLICK / COPY LINK: " + resetLink);
        System.out.println("=========================================================================");

        String subject = "Reset Your Password - Blood Bank Management System";

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 25px; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #ffffff;'>"
                + "<h2 style='color: #b30000; text-align: center; margin-bottom: 20px;'>🩸 Blood Bank Management System</h2>"
                + "<h3 style='color: #333333; margin-top: 0;'>Password Reset Request</h3>"
                + "<p style='color: #555555; font-size: 15px;'>Hello <strong>" + user.getFullName() + "</strong>,</p>"
                + "<p style='color: #555555; font-size: 15px;'>We received a request to reset the password for your account (<strong>" + user.getEmail() + "</strong>).</p>"
                + "<p style='color: #555555; font-size: 15px;'>Please click the button below to reset your password:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + resetLink + "' style='background-color: #b30000; color: #ffffff; padding: 12px 26px; text-decoration: none; font-weight: bold; border-radius: 6px; display: inline-block; font-size: 16px; box-shadow: 0 4px 6px rgba(179,0,0,0.2);'>Reset Password</a>"
                + "</div>"
                + "<p style='color: #555555; font-size: 14px;'>Or copy and paste this link into your browser address bar:</p>"
                + "<p style='word-break: break-all; font-size: 14px;'><a href='" + resetLink + "' style='color: #0288d1;'>" + resetLink + "</a></p>"
                + "<p style='color: #777777; font-size: 13px; margin-top: 25px;'>This reset link is valid for <strong>30 minutes</strong>. If you did not request a password reset, please ignore this email.</p>"
                + "<hr style='border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;'>"
                + "<p style='color: #999999; font-size: 12px; text-align: center;'>Blood Bank Management System &copy; 2026</p>"
                + "</div>";

        try {
            emailService.sendHtmlEmail(user.getEmail(), subject, htmlContent);
        } catch (Exception e) {
            System.err.println("Password reset email dispatch warning: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void resetPassword(String token, String newPassword, String confirmPassword) {

        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Invalid password reset token.");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token."));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Password reset link has expired. Please request a new link.");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("New password is required.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirm password do not match.");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must contain at least 6 characters.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }

}