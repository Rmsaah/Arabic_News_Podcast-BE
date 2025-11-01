package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;

@Data // Provides Getters, Setters, toString, etc.
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequestDto {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
