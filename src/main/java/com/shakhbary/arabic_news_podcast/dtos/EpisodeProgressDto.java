package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeProgressDto {
    private UUID id;
    private UUID episodeId;
    private String episodeTitle;
    private long lastPositionSeconds;        // Primary data: exact second (stored in DB)
    private double completionPercentage;     // Calculated: position/duration (NOT stored)
    private boolean isCompleted;             // Analytics: did user finish?
    private int playCount;                   // Analytics: how many times played
    private OffsetDateTime lastPlayedDate;   // When last updated/played
    private long remainingSeconds;           // Calculated: time left
    private String formattedPosition;        // Formatted: "12:45" or "1:23:45"
    private String formattedRemaining;       // Formatted: "7:15 remaining"

    // Constructor for API responses with calculated fields
    public EpisodeProgressDto(UUID id, UUID episodeId, String episodeTitle,
                              long lastPositionSeconds, double completionPercentage,
                              boolean isCompleted, int playCount, OffsetDateTime lastPlayedDate) {
        this.id = id;
        this.episodeId = episodeId;
        this.episodeTitle = episodeTitle;
        this.lastPositionSeconds = lastPositionSeconds;
        this.completionPercentage = completionPercentage;
        this.isCompleted = isCompleted;
        this.playCount = playCount;
        this.lastPlayedDate = lastPlayedDate;
    }
}
