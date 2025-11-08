package com.shakhbary.arabic_news_podcast.services;

import java.util.UUID;

import com.shakhbary.arabic_news_podcast.dtos.UserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserRegistrationRequestDto;

/**
 * Service for managing user account operations
 */
public interface UserService {

    /**
     * Register a new user in the system.
     * Assigns default ROLE_USER and encrypts password.
     *
     * @param registrationRequest User registration data
     * @return UserDto containing user details (excluding password)
     * @throws RuntimeException if username or email already exists
     */
    UserDto registerNewUser(UserRegistrationRequestDto registrationRequest);

    /**
     * Retrieve user information by their unique identifier.
     * Validates that the requesting user has permission to view this profile.
     *
     * @param userId The unique identifier of the user
     * @param requestingUsername The username of the user making the request
     * @return UserDto containing user details
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    UserDto getUser(UUID userId, String requestingUsername);

    /**
     * Update a user's first and last name.
     * Validates that the requesting user has permission to update this profile.
     *
     * @param userId The unique identifier of the user
     * @param firstName The new first name
     * @param lastName The new last name
     * @param requestingUsername The username of the user making the request
     * @return Updated UserDto with new name information
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    UserDto updateUserName(UUID userId, String firstName, String lastName, String requestingUsername);
}
