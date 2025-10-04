package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    // User info
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private OffsetDateTime createdAt;

    // Stats
    private int totalEpisodesCompleted;
    private long totalSecondsListened;

    // Recent ratings (simplified)
    private List<UserRatingDto> recentRatings;

    // Episode history (completed episodes with optional ratings)
    private List<EpisodeHistoryDto> episodeHistory;

    /**
     * Nested DTO representing a user's rating in the context of their profile.
     * This is nested because it's specific to UserProfileDto and not reusable elsewhere.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRatingDto {
        private UUID episodeId;
        private String episodeTitle;
        private int rating;
        private OffsetDateTime ratedAt;
    }
}
