package com.example.demo.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserResponse> getUsers(int page, int size, String keyword, String sortBy, String direction) {
        String validSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(validSortBy).descending()
                : Sort.by(validSortBy).ascending();

        Pageable pageable = PageRequest.of(page < 0 ? 0 : page, size <= 0 ? 10 : size, sort);

        Page<User> usersPage;
        if (keyword == null || keyword.isBlank()) {
            usersPage = userRepository.findAll(pageable);
        } else {
            String trimmed = keyword.trim();
            usersPage = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(trimmed, trimmed, pageable);
        }

        return usersPage.map(UserMapper::toResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return UserMapper.toResponse(user);
    }

    @Transactional
    @Override
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new InvalidRequestException("Full Name is required.");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidRequestException("Email address is required.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (!normalizedEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new DuplicateResourceException("Email '" + normalizedEmail + "' is already in use by another user.");
            }
            user.setEmail(normalizedEmail);
        }

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            String roleName = request.getRole().trim().toUpperCase();
            
            Role role = roleRepository.findByRoleNameIgnoreCase(roleName)
                    .orElseGet(() -> roleRepository.findByRoleNameIgnoreCase(
                            roleName.startsWith("ROLE_") ? roleName.substring(5) : "ROLE_" + roleName)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setRoleName(roleName);
                        return roleRepository.save(newRole);
                    }));
            user.setRole(role);
        }

        userRepository.save(user);

        // Record Audit Log
        auditLogService.saveLog(
                "Admin",
                "UPDATE",
                "User",
                "Updated user : " + user.getEmail());
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        userRepository.deleteById(id);

        // Record Audit Log
        auditLogService.saveLog(
                "Admin",
                "DELETE",
                "User",
                "Deleted user : " + user.getEmail());
    }

    @Override
    public void enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public List<UserResponse> searchUsers(String keyword) {
        List<User> users;
        if (keyword == null || keyword.trim().isEmpty()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByFullNameContainingIgnoreCase(keyword);
            if (users.isEmpty()) {
                users = userRepository.findByEmailContainingIgnoreCase(keyword);
            }
            if (users.isEmpty()) {
                users = userRepository.findByPhoneContaining(keyword);
            }
        }
        return users.stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

}