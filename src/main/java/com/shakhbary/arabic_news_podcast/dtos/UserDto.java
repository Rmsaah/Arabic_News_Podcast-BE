package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastLoginAt;
}