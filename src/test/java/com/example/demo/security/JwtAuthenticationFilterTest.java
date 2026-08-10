package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    // =========================================================
    // NO AUTHORIZATION HEADER
    // =========================================================

    @Test
    void doFilter_shouldContinue_whenAuthorizationHeaderIsMissing()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        verify(filterChain, times(1))
                .doFilter(request, response);

        verify(jwtService, never())
                .extractUsername(any());

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    // =========================================================
    // INVALID AUTHORIZATION HEADER
    // =========================================================

    @Test
    void doFilter_shouldContinue_whenAuthorizationHeaderDoesNotContainBearer()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        verify(filterChain, times(1))
                .doFilter(request, response);

        verify(jwtService, never())
                .extractUsername(any());

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    // =========================================================
    // VALID JWT
    // =========================================================

    @Test
    void doFilter_shouldAuthenticateUser_whenJwtIsValid()
            throws ServletException, IOException {

        String token = "valid.jwt.token";
        String username = "nagaraja@gmail.com";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token))
                .thenReturn(username);

        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid(
                token,
                userDetails))
                .thenReturn(true);

        when(userDetails.getAuthorities())
                .thenReturn(java.util.Collections.emptyList());

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        verify(jwtService, times(1))
                .extractUsername(token);

        verify(userDetailsService, times(1))
                .loadUserByUsername(username);

        verify(jwtService, times(1))
                .isTokenValid(token, userDetails);

        verify(filterChain, times(1))
                .doFilter(request, response);

        assertNotNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());

        assertNotNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal());
    }

    // =========================================================
    // INVALID JWT
    // =========================================================

    @Test
    void doFilter_shouldNotAuthenticate_whenJwtIsInvalid()
            throws ServletException, IOException {

        String token = "invalid.jwt.token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token))
                .thenThrow(
                        new RuntimeException(
                                "Invalid JWT token"));

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        verify(jwtService, times(1))
                .extractUsername(token);

        verify(userDetailsService, never())
                .loadUserByUsername(any());

        verify(filterChain, times(1))
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    // =========================================================
    // JWT VALID BUT TOKEN VALIDATION FAILS
    // =========================================================

    @Test
    void doFilter_shouldNotAuthenticate_whenTokenValidationFails()
            throws ServletException, IOException {

        String token = "expired.jwt.token";
        String username = "nagaraja@gmail.com";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token))
                .thenReturn(username);

        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid(
                token,
                userDetails))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        verify(jwtService, times(1))
                .extractUsername(token);

        verify(userDetailsService, times(1))
                .loadUserByUsername(username);

        verify(jwtService, times(1))
                .isTokenValid(token, userDetails);

        verify(filterChain, times(1))
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    // =========================================================
    // USER NOT FOUND
    // =========================================================

    @Test
    void doFilter_shouldNotAuthenticate_whenUserDoesNotExist()
            throws ServletException, IOException {

        String token = "valid.jwt.token";
        String username = "unknown@gmail.com";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token))
                .thenReturn(username);

        when(userDetailsService.loadUserByUsername(username))
                .thenThrow(
                        new RuntimeException(
                                "User not found"));

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        verify(jwtService, times(1))
                .extractUsername(token);

        verify(userDetailsService, times(1))
                .loadUserByUsername(username);

        verify(jwtService, never())
                .isTokenValid(
                        any(),
                        any());

        verify(filterChain, times(1))
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    // =========================================================
    // EXISTING AUTHENTICATION
    // =========================================================

    @Test
    void doFilter_shouldNotAuthenticateAgain_whenAuthenticationAlreadyExists()
            throws ServletException, IOException {

        String token = "valid.jwt.token";

        UsernamePasswordAuthenticationToken existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        "existing-user",
                        null,
                        java.util.Collections.emptyList());

        SecurityContextHolder
                .getContext()
                .setAuthentication(existingAuthentication);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token))
                .thenReturn("nagaraja@gmail.com");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        verify(jwtService, times(1))
                .extractUsername(token);

        verify(userDetailsService, never())
                .loadUserByUsername(any());

        verify(jwtService, never())
                .isTokenValid(
                        any(),
                        any());

        verify(filterChain, times(1))
                .doFilter(request, response);

        assertNotNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getCredentials());
    }
}
