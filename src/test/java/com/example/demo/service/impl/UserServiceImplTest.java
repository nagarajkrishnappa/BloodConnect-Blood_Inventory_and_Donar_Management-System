
package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuditLogService;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Test
    void getAllUsers_shouldReturnAllUsers_whenUsersExist() {

        // Arrange

        User user1 = new User();
        user1.setEmail("user1@gmail.com");
        user1.setFullName("User One");

        User user2 = new User();
        user2.setEmail("user2@gmail.com");
        user2.setFullName("User Two");

        when(userRepository.findAll())
                .thenReturn(Arrays.asList(user1, user2));

        // Act

        List<UserResponse> result = userService.getAllUsers();

        // Assert

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(userRepository, times(1))
                .findAll();
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsersExist() {

        // Arrange

        when(userRepository.findAll())
                .thenReturn(Collections.emptyList());

        // Act

        List<UserResponse> result = userService.getAllUsers();

        // Assert

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1))
                .findAll();
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {

        // Arrange

        Long userId = 1L;

        User user = new User();
        user.setEmail("nagaraja@gmail.com");
        user.setFullName("Nagaraja");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act

        UserResponse result = userService.getUserById(userId);

        // Assert

        assertNotNull(result);
        assertEquals("nagaraja@gmail.com", result.getEmail());

        verify(userRepository, times(1))
                .findById(userId);
    }

    @Test
    void getUserById_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(userId));

        assertEquals(
                "User not found with ID: 999",
                exception.getMessage());

        verify(userRepository, times(1))
                .findById(userId);
    }

    // =========================================================
    // UPDATE USER
    // =========================================================

    @Test
    void updateUser_shouldUpdateUser_whenValidRequest() {

        // Arrange

        Long userId = 1L;

        User user = new User();

        user.setFullName("Old Name");
        user.setEmail("old@gmail.com");
        user.setPhone("9999999999");
        user.setEnabled(false);

        UserUpdateRequest request = new UserUpdateRequest();

        request.setFullName("Nagaraja");
        request.setEmail("nagaraja@gmail.com");
        request.setPhone("9876543210");
        request.setEnabled(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("nagaraja@gmail.com"))
                .thenReturn(false);

        // Act

        userService.updateUser(userId, request);

        // Assert

        assertEquals(
                "Nagaraja",
                user.getFullName());

        assertEquals(
                "nagaraja@gmail.com",
                user.getEmail());

        assertEquals(
                "9876543210",
                user.getPhone());

        assertTrue(user.getEnabled());

        verify(userRepository, times(1))
                .findById(userId);

        verify(userRepository, times(1))
                .existsByEmail("nagaraja@gmail.com");

        verify(userRepository, times(1))
                .save(user);
    }

    @Test
    void updateUser_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        Long userId = 999L;

        UserUpdateRequest request = new UserUpdateRequest();

        request.setFullName("Nagaraja");
        request.setEmail("nagaraja@gmail.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(userId, request));

        assertEquals(
                "User not found with ID: 999",
                exception.getMessage());

        verify(userRepository, times(1))
                .findById(userId);

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowException_whenFullNameIsMissing() {

        // Arrange

        Long userId = 1L;

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");

        UserUpdateRequest request = new UserUpdateRequest();

        request.setFullName(" ");
        request.setEmail("nagaraja@gmail.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(userId, request));

        assertEquals(
                "Full Name is required.",
                exception.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowException_whenEmailIsMissing() {

        // Arrange

        Long userId = 1L;

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");

        UserUpdateRequest request = new UserUpdateRequest();

        request.setFullName("Nagaraja");
        request.setEmail(" ");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(userId, request));

        assertEquals(
                "Email address is required.",
                exception.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowException_whenEmailAlreadyExists() {

        // Arrange

        Long userId = 1L;

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("old@gmail.com");

        UserUpdateRequest request = new UserUpdateRequest();

        request.setFullName("Nagaraja");
        request.setEmail("existing@gmail.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("existing@gmail.com"))
                .thenReturn(true);

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(userId, request));

        assertEquals(
                "Email 'existing@gmail.com' is already in use by another user.",
                exception.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void updateUser_shouldKeepEmail_whenSameEmailIsProvided() {

        // Arrange

        Long userId = 1L;

        User user = new User();

        user.setFullName("Old Name");
        user.setEmail("nagaraja@gmail.com");

        UserUpdateRequest request = new UserUpdateRequest();

        request.setFullName("Nagaraja");
        request.setEmail("nagaraja@gmail.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act

        userService.updateUser(userId, request);

        // Assert

        assertEquals(
                "nagaraja@gmail.com",
                user.getEmail());

        assertEquals(
                "Nagaraja",
                user.getFullName());

        verify(userRepository, never())
                .existsByEmail(anyString());

        verify(userRepository, times(1))
                .save(user);
    }

    @Test
    void updateUser_shouldUpdateRole_whenValidRoleProvided() {

        // Arrange

        Long userId = 1L;

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");

        UserUpdateRequest request = new UserUpdateRequest();

        request.setFullName("Nagaraja");
        request.setEmail("nagaraja@gmail.com");
        request.setRole("ADMIN");

        Role role = new Role();

        role.setRoleName("ADMIN");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByRoleNameIgnoreCase("ADMIN"))
                .thenReturn(Optional.of(role));

        // Act

        userService.updateUser(userId, request);

        // Assert

        assertNotNull(user.getRole());

        assertEquals(
                "ADMIN",
                user.getRole().getRoleName());

        verify(roleRepository, times(1))
                .findByRoleNameIgnoreCase("ADMIN");

        verify(userRepository, times(1))
                .save(user);
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {

        // Arrange

        Long userId = 4L;

        User user = new User();
        user.setId(userId);
        user.setEmail("nagaraja@gmail.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act

        userService.deleteUser(userId);

        // Assert

        verify(userRepository, times(1))
                .findById(userId);

        verify(userRepository, times(1))
                .deleteById(userId);

        verify(auditLogService, times(1))
                .saveLog(
                        eq("Admin"),
                        eq("DELETE"),
                        eq("User"),
                        anyString());
    }

    @Test
    void deleteUser_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.deleteUser(userId));

        assertEquals(
                "User not found with ID: 999",
                exception.getMessage());

        verify(userRepository, times(1))
                .findById(userId);

        verify(userRepository, never())
                .deleteById(anyLong());
    }

    // =========================================================
    // ENABLE USER
    // =========================================================

    @Test
    void enableUser_shouldEnableUser_whenUserExists() {

        // Arrange

        Long userId = 1L;

        User user = new User();

        user.setEnabled(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act

        userService.enableUser(userId);

        // Assert

        assertTrue(user.getEnabled());

        verify(userRepository, times(1))
                .findById(userId);

        verify(userRepository, times(1))
                .save(user);
    }

    @Test
    void enableUser_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.enableUser(userId));

        assertEquals(
                "User not found with ID: 999",
                exception.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // DISABLE USER
    // =========================================================

    @Test
    void disableUser_shouldDisableUser_whenUserExists() {

        // Arrange

        Long userId = 1L;

        User user = new User();

        user.setEnabled(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act

        userService.disableUser(userId);

        // Assert

        assertFalse(user.getEnabled());

        verify(userRepository, times(1))
                .findById(userId);

        verify(userRepository, times(1))
                .save(user);
    }

    @Test
    void disableUser_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.disableUser(userId));

        assertEquals(
                "User not found with ID: 999",
                exception.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================================
    // SEARCH USERS
    // =========================================================

    @Test
    void searchUsers_shouldReturnAllUsers_whenKeywordIsEmpty() {

        // Arrange

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");

        when(userRepository.findAll())
                .thenReturn(Collections.singletonList(user));

        // Act

        List<UserResponse> result = userService.searchUsers("");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1))
                .findAll();
    }

    @Test
    void searchUsers_shouldSearchByFullName_whenKeywordMatchesName() {

        // Arrange

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");

        when(userRepository.findByFullNameContainingIgnoreCase("Nagaraja"))
                .thenReturn(Collections.singletonList(user));

        // Act

        List<UserResponse> result = userService.searchUsers("Nagaraja");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1))
                .findByFullNameContainingIgnoreCase("Nagaraja");

        verify(userRepository, never())
                .findByEmailContainingIgnoreCase(anyString());

        verify(userRepository, never())
                .findByPhoneContaining(anyString());
    }

    @Test
    void searchUsers_shouldSearchByEmail_whenNameSearchReturnsEmpty() {

        // Arrange

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");

        when(userRepository.findByFullNameContainingIgnoreCase("gmail"))
                .thenReturn(Collections.emptyList());

        when(userRepository.findByEmailContainingIgnoreCase("gmail"))
                .thenReturn(Collections.singletonList(user));

        // Act

        List<UserResponse> result = userService.searchUsers("gmail");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1))
                .findByFullNameContainingIgnoreCase("gmail");

        verify(userRepository, times(1))
                .findByEmailContainingIgnoreCase("gmail");

        verify(userRepository, never())
                .findByPhoneContaining(anyString());
    }

    @Test
    void searchUsers_shouldSearchByPhone_whenNameAndEmailSearchReturnEmpty() {

        // Arrange

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");
        user.setPhone("9876543210");

        when(userRepository.findByFullNameContainingIgnoreCase("9876543210"))
                .thenReturn(Collections.emptyList());

        when(userRepository.findByEmailContainingIgnoreCase("9876543210"))
                .thenReturn(Collections.emptyList());

        when(userRepository.findByPhoneContaining("9876543210"))
                .thenReturn(Collections.singletonList(user));

        // Act

        List<UserResponse> result = userService.searchUsers("9876543210");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1))
                .findByFullNameContainingIgnoreCase("9876543210");

        verify(userRepository, times(1))
                .findByEmailContainingIgnoreCase("9876543210");

        verify(userRepository, times(1))
                .findByPhoneContaining("9876543210");
    }
}
