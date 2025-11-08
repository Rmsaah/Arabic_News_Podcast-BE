package com.shakhbary.arabic_news_podcast.services;

import java.util.UUID;

import com.shakhbary.arabic_news_podcast.dtos.UserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserRegistrationRequestDto;
import com.shakhbary.arabic_news_podcast.models.User;

/**
 * Service for managing user account operations
 */
public interface UserService {

    /**
     * Register a new user in the system.
     * Assigns default ROLE_USER and encrypts password.
     *
     * @param registrationRequest User registration data
     * @return The created User entity
     * @throws RuntimeException if username or email already exists
     */
    User registerNewUser(UserRegistrationRequestDto registrationRequest);

    /**
     * Retrieve user information by their unique identifier
     * @param userId The unique identifier of the user
     * @return UserDto containing user details
     */
    UserDto getUser(UUID userId);

    /**
     * Update a user's first and last name
     * @param userId The unique identifier of the user
     * @param firstName The new first name
     * @param lastName The new last name
     * @return Updated UserDto with new name information
     */
    UserDto updateUserName(UUID userId, String firstName, String lastName);
}