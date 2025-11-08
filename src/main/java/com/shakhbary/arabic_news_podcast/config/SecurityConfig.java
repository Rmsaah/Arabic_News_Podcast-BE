package com.shakhbary.arabic_news_podcast.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration with authentication and CORS support.
 *
 * Features:
 * - Basic Authentication with username/password
 * - Role-based access control (ADMIN, USER)
 * - Password encryption with BCrypt
 * - CORS enabled for Angular frontend
 * - Stateless session management (REST API)
 *
 * Authentication:
 * - Uses CustomUserDetailsService to load users from database
 * - Passwords are hashed with BCrypt
 * - Basic Auth header: Authorization: Basic base64(username:password)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS for Angular frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Disable CSRF (standard for stateless REST APIs with Basic Auth)
                .csrf(csrf -> csrf.disable())

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/register").permitAll()       // User registration
                        .requestMatchers("/api/episodes/**").permitAll()         // Browse episodes
                        .requestMatchers("/api/audio/**").permitAll()            // Stream audio
                        .requestMatchers("/api/home/**").permitAll()             // Homepage content

                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // User endpoints - require USER or ADMIN role
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/progress/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/ratings/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")

                        // All other requests are public (for development)
                        // Change to .authenticated() for production if needed
                        .anyRequest().permitAll()
                )

                // Enable HTTP Basic Authentication
                .httpBasic(Customizer.withDefaults())

                // Stateless session management (no server-side sessions)
                // Each request must include authentication credentials
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    /**
     * Password encoder for hashing user passwords.
     * Uses BCrypt algorithm with strength 10 (default).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager for authenticating users.
     * Configured to use CustomUserDetailsService and BCrypt password encoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());

        return authBuilder.build();
    }
}
