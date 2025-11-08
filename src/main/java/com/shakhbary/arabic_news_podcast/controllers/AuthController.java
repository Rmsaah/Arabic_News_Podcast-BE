package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.UserRegistrationRequestDto;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Handles user registration and related authentication tasks.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Register a new user.
     * This endpoint is public to allow new users to register.
     * Assigns ROLE_USER by default.
     *
     * @param registrationRequest User registration data
     * @return Success message with username
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegistrationRequestDto registrationRequest) {
        try {
            User newUser = userService.registerNewUser(registrationRequest);
            // In a real app, you wouldn't return the entire user object, especially the password,
            // but for a successful test, it's sufficient.
            return new ResponseEntity<>(
                    "User registered successfully: " + newUser.getUsername(),
                    HttpStatus.CREATED
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
