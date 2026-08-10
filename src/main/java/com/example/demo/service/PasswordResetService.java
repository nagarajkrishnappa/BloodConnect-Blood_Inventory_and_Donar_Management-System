package com.example.demo.service;

public interface PasswordResetService {

    void requestPasswordReset(String email);

    void resetPassword(
            String token,
            String newPassword,
            String confirmPassword);

}