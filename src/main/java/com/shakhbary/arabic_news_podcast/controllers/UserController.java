package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.UpdateUserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserProfileDto;
import com.shakhbary.arabic_news_podcast.services.UserProfileService;
import com.shakhbary.arabic_news_podcast.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


/**
 * REST controller for managing users and user profiles.
 * Handles user information retrieval and updates.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    /**
     * Get user information by ID.
     * Users can only view their own profile unless they have ADMIN role.
     *
     * @param id User ID
     * @param authentication Current authenticated user
     * @return User information
     */
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable("id") UUID id, Authentication authentication) {
        return userService.getUser(id, authentication.getName());
    }

    /**
     * Update user's name (first name and/or last name).
     * Users can only update their own profile.
     * At least one field must be provided in the request.
     *
     * @param id User ID
     * @param updateDto Update data containing firstName and/or lastName
     * @param authentication Current authenticated user
     * @return Updated user information
     */
    @PatchMapping("/{id}")
    public UserDto updateUserName(
            @PathVariable("id") UUID id,
            @RequestBody @Valid UpdateUserDto updateDto,
            Authentication authentication) {

        updateDto.validateNotEmpty();

        return userService.updateUserName(
                id,
                updateDto.firstName(),
                updateDto.lastName(),
                authentication.getName()
        );
    }

    /**
     * Get user's complete profile including listening statistics and ratings.
     * Users can only view their own profile unless they have ADMIN role.
     *
     * @param id User ID
     * @param authentication Current authenticated user
     * @return Complete user profile with stats
     */
    @GetMapping("/{id}/profile")
    public UserProfileDto getProfile(@PathVariable("id") UUID id, Authentication authentication) {
        return userProfileService.getUserProfile(id, authentication.getName());
    }
}
