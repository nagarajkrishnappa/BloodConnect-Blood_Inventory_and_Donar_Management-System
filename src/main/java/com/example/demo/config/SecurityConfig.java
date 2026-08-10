package com.example.demo.config;

import com.example.demo.security.CustomAuthenticationSuccessHandler;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ==========================================
    // Authentication Provider
    // ==========================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    // ==========================================
    // Authentication Manager
    // ==========================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig)
            throws Exception {

        return authConfig.getAuthenticationManager();
    }

    // ==========================================
    // Security Filter Chain
    // ==========================================

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {

        http

                // ==========================================
                // Authentication Provider
                // ==========================================

                .authenticationProvider(authenticationProvider())

                // ==========================================
                // Disable CSRF
                // ==========================================

                .csrf(csrf -> csrf.disable())

                // ==========================================
                // Exception Handling for REST APIs
                // 401 Unauthorized -> Unauthenticated (No/Invalid Token)
                // 403 Forbidden    -> Access Denied (Wrong Role)
                // ==========================================

                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                AntPathRequestMatcher.antMatcher("/api/**")
                        )
                        .defaultAccessDeniedHandlerFor(
                                (request, response, accessDeniedException) ->
                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN),
                                AntPathRequestMatcher.antMatcher("/api/**")
                        )
                )

                // ==========================================
                // Authorization Rules
                // ==========================================

                .authorizeHttpRequests(auth -> auth

                        // ==================================
                        // 1. PUBLIC REST APIs & WEB PAGES
                        // ==================================

                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/login"),
                                AntPathRequestMatcher.antMatcher("/register"),
                                AntPathRequestMatcher.antMatcher("/forgot-password"),
                                AntPathRequestMatcher.antMatcher("/reset-password"),
                                AntPathRequestMatcher.antMatcher("/css/**"),
                                AntPathRequestMatcher.antMatcher("/js/**"),
                                AntPathRequestMatcher.antMatcher("/images/**"),
                                AntPathRequestMatcher.antMatcher("/api/auth/register"),
                                AntPathRequestMatcher.antMatcher("/api/auth/login"))
                        .permitAll()

                        // ==================================
                        // 2. ADMIN-ONLY USER MANAGEMENT & ADMIN PATHS
                        // ==================================

                        .requestMatchers(
                                "/api/users",
                                "/api/users/**",
                                "/api/admin/**",
                                "/admin/**")
                        .hasRole("ADMIN")

                        // ==================================
                        // 3. ADMIN-ONLY ACTIONS (Approving/Rejecting requests & Modifying Blood Stock)
                        // ==================================

                        .requestMatchers(
                                "/api/bloodrequests/*/approve",
                                "/api/bloodrequests/*/reject")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/bloodstock/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/bloodstock/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/bloodstock/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/donors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/donors/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/donations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/donations/**").hasRole("ADMIN")

                        // ==================================
                        // 4. USER & ADMIN ACCESSIBLE ENDPOINTS
                        // (GET bloodstock, GET/POST donors, GET/POST bloodrequests, GET/POST donations)
                        // ==================================

                        .requestMatchers("/user/**").hasRole("USER")

                        // ==================================
                        // 5. ALL OTHER REST APIs
                        // (Requires valid JWT - USER or ADMIN)
                        // ==================================

                        .requestMatchers("/api/**")
                        .authenticated()

                        // ==================================
                        // EVERYTHING ELSE
                        // ==================================

                        .anyRequest()
                        .authenticated())

                // ==========================================
                // FORM LOGIN
                // ==========================================

                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll())

                // ==========================================
                // LOGOUT
                // ==========================================

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())

                // ==========================================
                // JWT FILTER
                // ==========================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}