package com.shakhbary.arabic_news_podcast.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for user login requests. Simple username/password validation. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

  @NotBlank(message = "Username is required")
  private String username;

  @NotBlank(message = "Password is required")
  private String password;
}
