package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.UserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserRegistrationRequestDto;
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
     * @return UserDto with full user information (excluding password)
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody @Valid UserRegistrationRequestDto registrationRequest) {
        UserDto newUser = userService.registerNewUser(registrationRequest);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
}
