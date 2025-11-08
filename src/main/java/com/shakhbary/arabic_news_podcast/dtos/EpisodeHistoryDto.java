package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO representing a user's interaction with an episode in their history
 * Includes both completed and in-progress episodes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeHistoryDto {
    // Episode basic info
    private UUID episodeId;
    private String episodeTitle;
    private String episodeImageUrl;

    // Progress tracking
    private long lastPositionSeconds;       // Where user stopped
    private double completionPercentage;    // 0.0 to 1.0
    private boolean isCompleted;            // True if user finished the episode
    private int playCount;                  // How many times user played this episode
    private OffsetDateTime lastPlayedDate;  // When user last interacted with this episode

    // Rating information
    private String ratingStatus;            // "Not Rated" or "1 star", "2 stars", etc.
    private Integer ratingValue;            // Null if not rated, 1-5 if rated
    private OffsetDateTime ratingDate;      // When user rated this episode (null if not rated)
}
