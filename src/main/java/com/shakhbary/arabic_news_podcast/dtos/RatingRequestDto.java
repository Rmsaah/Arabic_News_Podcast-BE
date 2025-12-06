package com.shakhbary.arabic_news_podcast.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for rating an episode. Used when a user submits a rating for a podcast episode. User is
 * determined from authentication token.
 */
public record RatingRequestDto(
    @NotNull(message = "Episode ID is required") UUID episodeId,
    @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating) {}
