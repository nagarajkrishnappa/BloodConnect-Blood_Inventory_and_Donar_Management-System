package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.EmailService;

import com.example.demo.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    // =========================================================
    // REGISTER TESTS
    // =========================================================

    @Test
    void register_shouldCreateUser_whenValidRequest() {

        // Arrange

        RegisterRequest request = new RegisterRequest();

        request.setFullName("Nagaraja");
        request.setEmail("nagaraja@gmail.com");
        request.setPassword("Password@123");
        request.setPhone("9876543210");

        Role role = new Role();
        role.setRoleName("USER");

        when(userRepository.existsByEmail("nagaraja@gmail.com"))
                .thenReturn(false);

        when(roleRepository.findByRoleNameIgnoreCase("USER"))
                .thenReturn(Optional.of(role));

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encodedPassword");

        // Act

        authService.register(request);

        // Assert

        verify(userRepository, times(1))
                .save(any(User.class));

        verify(passwordEncoder, times(1))
                .encode("Password@123");

        verify(emailService, times(1))
                .sendEmail(
                        eq("nagaraja@gmail.com"),
                        eq("Welcome to Blood Bank Management System"),
                        anyString());

        verify(auditLogService, times(1))
                .saveLog(
                        eq("nagaraja@gmail.com"),
                        eq("REGISTER"),
                        eq("Authentication"),
                        anyString());
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {

        // Arrange

        RegisterRequest request = new RegisterRequest();

        request.setFullName("Nagaraja");
        request.setEmail("nagaraja@gmail.com");
        request.setPassword("Password@123");

        when(userRepository.existsByEmail("nagaraja@gmail.com"))
                .thenReturn(true);

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request));

        assertEquals(
                "Email address is already registered. Please login.",
                exception.getMessage());

        // User must not be saved

        verify(userRepository, never())
                .save(any(User.class));

        // Password must not be encoded

        verify(passwordEncoder, never())
                .encode(anyString());

        // Email must not be sent

        verify(emailService, never())
                .sendEmail(
                        anyString(),
                        anyString(),
                        anyString());

        // Audit log must not be created

        verify(auditLogService, never())
                .saveLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString());
    }

    // =========================================================
    // LOGIN TESTS
    // =========================================================

    @Test
    void login_shouldReturnUser_whenCredentialsAreValid() {

        // Arrange

        LoginRequest request = new LoginRequest();

        request.setEmail("nagaraja@gmail.com");
        request.setPassword("Password@123");

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");
        user.setPassword("encodedPassword");
        user.setEnabled(true);

        when(userRepository.findByEmail("nagaraja@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password@123",
                "encodedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(any()))
                .thenReturn("mockToken");

        // Act

        var response = authService.login(request);

        // Assert

        assertEquals(
                "nagaraja@gmail.com",
                response.getUser().getEmail());

        verify(userRepository, times(1))
                .findByEmail("nagaraja@gmail.com");

        verify(passwordEncoder, times(1))
                .matches(
                        "Password@123",
                        "encodedPassword");

        verify(auditLogService, times(1))
                .saveLog(
                        eq("nagaraja@gmail.com"),
                        eq("LOGIN"),
                        eq("Authentication"),
                        anyString());
    }

    @Test
    void login_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        LoginRequest request = new LoginRequest();

        request.setEmail("unknown@gmail.com");
        request.setPassword("Password@123");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request));

        assertEquals(
                "Invalid email or password.",
                exception.getMessage());

        // Password should never be checked

        verify(passwordEncoder, never())
                .matches(
                        anyString(),
                        anyString());

        // Audit log should not be created

        verify(auditLogService, never())
                .saveLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString());
    }

    @Test
    void login_shouldThrowException_whenPasswordIsWrong() {

        // Arrange

        LoginRequest request = new LoginRequest();

        request.setEmail("nagaraja@gmail.com");
        request.setPassword("WrongPassword");

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");
        user.setPassword("encodedPassword");
        user.setEnabled(true);

        when(userRepository.findByEmail("nagaraja@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "encodedPassword"))
                .thenReturn(false);

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request));

        assertEquals(
                "Invalid email or password.",
                exception.getMessage());

        // Audit log should not be created

        verify(auditLogService, never())
                .saveLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString());
    }

    @Test
    void login_shouldThrowException_whenUserIsDisabled() {

        // Arrange

        LoginRequest request = new LoginRequest();

        request.setEmail("nagaraja@gmail.com");
        request.setPassword("Password@123");

        User user = new User();

        user.setFullName("Nagaraja");
        user.setEmail("nagaraja@gmail.com");
        user.setPassword("encodedPassword");
        user.setEnabled(false);

        when(userRepository.findByEmail("nagaraja@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password@123",
                "encodedPassword"))
                .thenReturn(true);

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request));

        assertEquals(
                "Your account is disabled. Please contact system administrator.",
                exception.getMessage());

        // Audit log should not be created

        verify(auditLogService, never())
                .saveLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString());
    }



// =========================================================
// REGISTER - NULL REQUEST
// =========================================================

@Test
void register_shouldThrowException_whenRequestIsNull() {

    // Act + Assert

    RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.register(null)
    );

    assertEquals(
            "Registration data cannot be null.",
            exception.getMessage()
    );

    verify(userRepository, never())
            .save(any(User.class));

    verify(emailService, never())
            .sendEmail(
                    anyString(),
                    anyString(),
                    anyString()
            );

    verify(auditLogService, never())
            .saveLog(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString()
            );
}


// =========================================================
// REGISTER - EMAIL NORMALIZATION
// =========================================================

@Test
void register_shouldNormalizeEmail_whenEmailContainsSpacesAndUppercase() {

    // Arrange

    RegisterRequest request = new RegisterRequest();

    request.setFullName("Nagaraja");
    request.setEmail("  NAGARAJA@GMAIL.COM  ");
    request.setPassword("Password@123");
    request.setPhone("9876543210");

    Role role = new Role();
    role.setRoleName("USER");

    when(userRepository.existsByEmail("nagaraja@gmail.com"))
            .thenReturn(false);

    when(roleRepository.findByRoleNameIgnoreCase("USER"))
            .thenReturn(Optional.of(role));

    when(passwordEncoder.encode("Password@123"))
            .thenReturn("encodedPassword");

    // Act

    authService.register(request);

    // Assert

    assertEquals(
            "nagaraja@gmail.com",
            request.getEmail()
    );

    verify(userRepository, times(1))
            .existsByEmail("nagaraja@gmail.com");

    verify(userRepository, times(1))
            .save(any(User.class));

    verify(emailService, times(1))
            .sendEmail(
                    eq("nagaraja@gmail.com"),
                    eq("Welcome to Blood Bank Management System"),
                    anyString()
            );
}


// =========================================================
// REGISTER - ROLE USER FALLBACK
// =========================================================

@Test
void register_shouldUseRoleUser_whenUserRoleExistsAsRoleUser() {

    // Arrange

    RegisterRequest request = new RegisterRequest();

    request.setFullName("Nagaraja");
    request.setEmail("nagaraja@gmail.com");
    request.setPassword("Password@123");

    Role role = new Role();
    role.setRoleName("ROLE_USER");

    when(userRepository.existsByEmail("nagaraja@gmail.com"))
            .thenReturn(false);

    when(roleRepository.findByRoleNameIgnoreCase("USER"))
            .thenReturn(Optional.empty());

    when(roleRepository.findByRoleNameIgnoreCase("ROLE_USER"))
            .thenReturn(Optional.of(role));

    when(passwordEncoder.encode("Password@123"))
            .thenReturn("encodedPassword");

    // Act

    authService.register(request);

    // Assert

    verify(roleRepository, times(1))
            .findByRoleNameIgnoreCase("USER");

    verify(roleRepository, times(1))
            .findByRoleNameIgnoreCase("ROLE_USER");

    verify(userRepository, times(1))
            .save(any(User.class));
}


// =========================================================
// REGISTER - CREATE USER ROLE IF ROLE DOES NOT EXIST
// =========================================================

@Test
void register_shouldCreateUserRole_whenRoleDoesNotExist() {

    // Arrange

    RegisterRequest request = new RegisterRequest();

    request.setFullName("Nagaraja");
    request.setEmail("nagaraja@gmail.com");
    request.setPassword("Password@123");

    when(userRepository.existsByEmail("nagaraja@gmail.com"))
            .thenReturn(false);

    when(roleRepository.findByRoleNameIgnoreCase("USER"))
            .thenReturn(Optional.empty());

    when(roleRepository.findByRoleNameIgnoreCase("ROLE_USER"))
            .thenReturn(Optional.empty());

    Role newRole = new Role();
    newRole.setRoleName("USER");

    when(roleRepository.save(any(Role.class)))
            .thenReturn(newRole);

    when(passwordEncoder.encode("Password@123"))
            .thenReturn("encodedPassword");

    // Act

    authService.register(request);

    // Assert

    verify(roleRepository, times(1))
            .save(any(Role.class));

    verify(userRepository, times(1))
            .save(any(User.class));

    verify(passwordEncoder, times(1))
            .encode("Password@123");
}


// =========================================================
// REGISTER - EMAIL SERVICE FAILURE
// =========================================================

@Test
void register_shouldStillComplete_whenWelcomeEmailFails() {

    // Arrange

    RegisterRequest request = new RegisterRequest();

    request.setFullName("Nagaraja");
    request.setEmail("nagaraja@gmail.com");
    request.setPassword("Password@123");

    Role role = new Role();
    role.setRoleName("USER");

    when(userRepository.existsByEmail("nagaraja@gmail.com"))
            .thenReturn(false);

    when(roleRepository.findByRoleNameIgnoreCase("USER"))
            .thenReturn(Optional.of(role));

    when(passwordEncoder.encode("Password@123"))
            .thenReturn("encodedPassword");

    // Email failure

    org.mockito.Mockito.doThrow(
            new RuntimeException("SMTP server unavailable")
    ).when(emailService).sendEmail(
            eq("nagaraja@gmail.com"),
            anyString(),
            anyString()
    );

    // Act

    authService.register(request);

    // Assert

    verify(userRepository, times(1))
            .save(any(User.class));

    verify(auditLogService, times(1))
            .saveLog(
                    eq("nagaraja@gmail.com"),
                    eq("REGISTER"),
                    eq("Authentication"),
                    anyString()
            );
}


// =========================================================
// LOGIN - NULL REQUEST
// =========================================================

@Test
void login_shouldThrowException_whenRequestIsNull() {

    // Act + Assert

    RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.login(null)
    );

    assertEquals(
            "Email address and password are required.",
            exception.getMessage()
    );

    verify(userRepository, never())
            .findByEmail(anyString());

    verify(passwordEncoder, never())
            .matches(
                    anyString(),
                    anyString()
            );

    verify(auditLogService, never())
            .saveLog(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString()
            );
}


// =========================================================
// LOGIN - NULL EMAIL
// =========================================================

@Test
void login_shouldThrowException_whenEmailIsNull() {

    // Arrange

    LoginRequest request = new LoginRequest();

    request.setEmail(null);
    request.setPassword("Password@123");

    // Act + Assert

    RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.login(request)
    );

    assertEquals(
            "Email address and password are required.",
            exception.getMessage()
    );

    verify(userRepository, never())
            .findByEmail(anyString());
}


// =========================================================
// LOGIN - NULL PASSWORD
// =========================================================

@Test
void login_shouldThrowException_whenPasswordIsNull() {

    // Arrange

    LoginRequest request = new LoginRequest();

    request.setEmail("nagaraja@gmail.com");
    request.setPassword(null);

    // Act + Assert

    RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.login(request)
    );

    assertEquals(
            "Email address and password are required.",
            exception.getMessage()
    );

    verify(userRepository, never())
            .findByEmail(anyString());
}


// =========================================================
// LOGIN - EMAIL NORMALIZATION
// =========================================================

@Test
void login_shouldNormalizeEmail_whenEmailContainsSpacesAndUppercase() {

    // Arrange

    LoginRequest request = new LoginRequest();

    request.setEmail("  NAGARAJA@GMAIL.COM  ");
    request.setPassword("Password@123");

    User user = new User();

    user.setFullName("Nagaraja");
    user.setEmail("nagaraja@gmail.com");
    user.setPassword("encodedPassword");
    user.setEnabled(true);

    when(userRepository.findByEmail("nagaraja@gmail.com"))
            .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(
            "Password@123",
            "encodedPassword"
    )).thenReturn(true);

    // Act

    var response = authService.login(request);

    // Assert

    assertNotNull(response);

    verify(userRepository, times(1))
            .findByEmail("nagaraja@gmail.com");

    verify(passwordEncoder, times(1))
            .matches(
                    "Password@123",
                    "encodedPassword"
            );

    verify(auditLogService, times(1))
            .saveLog(
                    eq("nagaraja@gmail.com"),
                    eq("LOGIN"),
                    eq("Authentication"),
                    anyString()
            );
}


// =========================================================
// LOGIN - JWT TOKEN GENERATION
// =========================================================

@Test
void login_shouldGenerateJwtToken_whenCredentialsAreValid() {

    // Arrange

    LoginRequest request = new LoginRequest();

    request.setEmail("nagaraja@gmail.com");
    request.setPassword("Password@123");

    User user = new User();

    user.setFullName("Nagaraja");
    user.setEmail("nagaraja@gmail.com");
    user.setPassword("encodedPassword");
    user.setEnabled(true);

    when(userRepository.findByEmail("nagaraja@gmail.com"))
            .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(
            "Password@123",
            "encodedPassword"
    )).thenReturn(true);

    when(jwtService.generateToken(any()))
            .thenReturn("mockToken");

    // Act

    var response = authService.login(request);

    // Assert

    assertNotNull(response);

    verify(jwtService, times(1))
            .generateToken(any());
}


}