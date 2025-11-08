package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDto {
    private UUID id;
    private String title;
    private String description;
    private String scriptUrlPath;
    private String audioUrlPath;
    private long durationSeconds;
    private double averageRating;
    private int ratingCount;
    private OffsetDateTime createdAt;
    private UUID articleId;
    private String articleTitle;
    private String imageUrl;

    // Constructor for list view (without transcript)
    public EpisodeDto(UUID id, String title, long durationSeconds, double averageRating,
                      int ratingCount, String imageUrl, String description, OffsetDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.durationSeconds = durationSeconds;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.imageUrl = imageUrl;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Constructor for creation response
    public EpisodeDto(UUID id, OffsetDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}
