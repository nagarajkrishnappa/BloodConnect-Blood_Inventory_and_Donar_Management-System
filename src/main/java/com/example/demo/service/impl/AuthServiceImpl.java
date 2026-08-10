package com.example.demo.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.AuthService;
import com.example.demo.service.EmailService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtService jwtService;

    // ==========================================
    // REGISTER USER
    // ==========================================

    @Override
    public void register(RegisterRequest request) {

        if (request == null) {
            throw new InvalidRequestException(
                    "Registration data cannot be null.");
        }

        String normalizedEmail =
                request.getEmail().trim().toLowerCase();

        request.setEmail(normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {

            throw new DuplicateResourceException(
                    "Email address is already registered. Please login.");
        }

        // Find USER role
        Role role = roleRepository
                .findByRoleNameIgnoreCase("USER")
                .orElseGet(() ->
                        roleRepository
                                .findByRoleNameIgnoreCase("ROLE_USER")
                                .orElseGet(() -> {

                                    Role newRole = new Role();

                                    newRole.setRoleName("USER");

                                    return roleRepository
                                            .save(newRole);
                                }));

        // Convert request to entity
        User user = UserMapper.toEntity(request, role);

        // Encrypt password
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()));

        user.setCreatedAt(LocalDateTime.now());

        user.setEnabled(true);

        // Save user
        userRepository.save(user);

        // ==========================================
        // AUDIT LOG
        // ==========================================

        auditLogService.saveLog(
                user.getEmail(),
                "REGISTER",
                "Authentication",
                "New user registered successfully.");

        // ==========================================
        // WELCOME EMAIL
        // ==========================================

        try {

            String subject =
                    "Welcome to Blood Bank Management System";

            String body =
                    "Hello " + user.getFullName() + ",\n\n"
                    + "Your account has been created successfully.\n\n"
                    + "Thank you for registering with our "
                    + "Blood Bank Management System.\n\n"
                    + "Account Email: " + user.getEmail() + "\n\n"
                    + "We appreciate your support in helping save lives.\n\n"
                    + "Regards,\n"
                    + "Blood Bank Management Team";

            emailService.sendEmail(
                    user.getEmail(),
                    subject,
                    body);

        } catch (Exception e) {

            System.err.println(
                    "Registration welcome email could not be sent to "
                    + user.getEmail()
                    + ": "
                    + e.getMessage());
        }
    }

    // ==========================================
    // LOGIN USER
    // ==========================================

    @Override
    public LoginResponse login(LoginRequest request) {

        if (request == null
                || request.getEmail() == null
                || request.getPassword() == null) {

            throw new InvalidRequestException(
                    "Email address and password are required.");
        }

        // Normalize email
        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        // Find user
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new InvalidRequestException(
                                "Invalid email or password."));

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidRequestException(
                    "Invalid email or password.");
        }

        // Check account status
        if (!Boolean.TRUE.equals(
                user.getEnabled())) {

            throw new InvalidRequestException(
                    "Your account is disabled. "
                    + "Please contact system administrator.");
        }

        // ==========================================
        // GENERATE JWT TOKEN
        // ==========================================

        String token =
                jwtService.generateToken(
                        new com.example.demo.security.UserPrincipal(user));

        // ==========================================
        // AUDIT LOG
        // ==========================================

        auditLogService.saveLog(
                user.getEmail(),
                "LOGIN",
                "Authentication",
                "User logged in via REST API.");

        // ==========================================
        // CREATE USER RESPONSE
        // ==========================================

        UserResponse userResponse =
                UserMapper.toResponse(user);

        // ==========================================
        // RETURN TOKEN + USER
        // ==========================================

        return new LoginResponse(
                token,
                userResponse);
    }
}