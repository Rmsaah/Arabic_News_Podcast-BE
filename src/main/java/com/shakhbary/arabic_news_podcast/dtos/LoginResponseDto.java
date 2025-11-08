package com.shakhbary.arabic_news_podcast.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response.
 * Contains user info and encoded credentials for Basic Auth.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    /**
     * User information (no password)
     */
    private UserDto user;

    /**
     * Base64-encoded credentials for Basic Auth header
     * Format: base64(username:password)
     * Client should store this and send as: Authorization: Basic {credentials}
     */
    private String credentials;

    /**
     * Authentication type (always "Basic" for this implementation)
     */
    private String authType;
}