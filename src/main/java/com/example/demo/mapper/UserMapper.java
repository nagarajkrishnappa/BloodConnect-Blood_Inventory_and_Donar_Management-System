package com.example.demo.mapper;

import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;

public class UserMapper {

    private UserMapper() {
    }

    // Convert RegisterRequest DTO to User Entity
    public static User toEntity(RegisterRequest request, Role role) {

        User user = new User();

        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : null);
        user.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null);
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);

        user.setEnabled(true);
        user.setRole(role);

        return user;
    }

    public static UserResponse toResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());

        if (user.getRole() != null) {
            response.setRole(user.getRole().getRoleName());
        } else {
            response.setRole("ROLE_USER");
        }

        response.setEnabled(user.getEnabled());

        return response;
    }

}
