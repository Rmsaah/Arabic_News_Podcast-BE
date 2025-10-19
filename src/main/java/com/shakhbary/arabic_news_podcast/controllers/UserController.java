package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.UpdateUserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserProfileDto;
import com.shakhbary.arabic_news_podcast.services.UserProfileService;
import com.shakhbary.arabic_news_podcast.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
     *
     * @param id User ID
     * @return User information
     */
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable("id") UUID id) {
        return userService.getUser(id);
    }

    /**
     * Update user's name (first name and/or last name).
     * At least one field must be provided in the request.
     *
     * @param id User ID
     * @param updateDto Update data containing firstName and/or lastName
     * @return Updated user information
     */
    @PatchMapping("/{id}")
    public UserDto updateUserName(
            @PathVariable("id") UUID id,
            @RequestBody @Valid UpdateUserDto updateDto) {

        updateDto.validateNotEmpty();

        return userService.updateUserName(
                id,
                updateDto.firstName(),
                updateDto.lastName()
        );
    }

    /**
     * Get user's complete profile including listening statistics and ratings.
     *
     * @param id User ID
     * @return Complete user profile with stats
     */
    @GetMapping("/{id}/profile")
    public UserProfileDto getProfile(@PathVariable("id") UUID id) {
        return userProfileService.getUserProfile(id);
    }
}