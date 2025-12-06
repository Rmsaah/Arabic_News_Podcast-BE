package com.shakhbary.arabic_news_podcast.dtos;

import com.shakhbary.arabic_news_podcast.exceptions.BadRequestException;
import jakarta.validation.constraints.Size;

/** DTO for updating user name fields. At least one field must be provided. */
public record UpdateUserDto(
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName) {
  /**
   * Check if the update contains any data. At least one field should be non-null.
   *
   * @return true if both fields are null
   */
  public boolean isEmpty() {
    return firstName == null && lastName == null;
  }

  /**
   * Validate that at least one field is provided.
   *
   * @throws BadRequestException if both fields are null
   */
  public void validateNotEmpty() {
    if (isEmpty()) {
      throw new BadRequestException("At least one field (firstName or lastName) must be provided");
    }
  }
}
