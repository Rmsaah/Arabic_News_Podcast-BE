package com.shakhbary.arabic_news_podcast.dtos;

import lombok.Data;

@Data // Provides Getters, Setters, toString, etc.
public class UserRegistrationRequest {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}