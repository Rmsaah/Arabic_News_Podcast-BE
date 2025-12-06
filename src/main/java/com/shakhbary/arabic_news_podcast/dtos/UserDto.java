package com.shakhbary.arabic_news_podcast.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
  private UUID id;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private OffsetDateTime creationDate;
  private OffsetDateTime lastLoginDate;
}
