package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;

public interface UserService {

    List<UserResponse> getAllUsers();

    Page<UserResponse> getUsers(int page, int size, String keyword, String sortBy, String direction);

    UserResponse getUserById(Long id);

    void updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    void enableUser(Long id);

    void disableUser(Long id);

    List<UserResponse> searchUsers(String keyword);

}