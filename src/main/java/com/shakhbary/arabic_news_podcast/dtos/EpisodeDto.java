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
    private OffsetDateTime creationDate;
    private UUID articleId;
    private String articleTitle;
    private String imageUrl;

    /**
     * Constructor for list view (without transcript).
     *
     * @deprecated This constructor is incomplete and omits critical fields like audioUrlPath.
     *             Frontend teams have reported issues with missing audio URLs when using this constructor.
     *             Use the full 12-parameter constructor or the all-args constructor instead.
     *             This constructor will be removed in a future version.
     *
     * @see #EpisodeDto(UUID, String, String, String, String, long, double, int, OffsetDateTime, UUID, String, String)
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public EpisodeDto(UUID id, String title, long durationSeconds, double averageRating,
                      int ratingCount, String imageUrl, String description, OffsetDateTime creationDate) {
        this.id = id;
        this.title = title;
        this.durationSeconds = durationSeconds;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.imageUrl = imageUrl;
        this.description = description;
        this.creationDate = creationDate;
        // WARNING: audioUrlPath, scriptUrlPath, articleId, and articleTitle are null!
    }

    // Constructor for creation response
    public EpisodeDto(UUID id, OffsetDateTime creationDate) {
        this.id = id;
        this.creationDate = creationDate;
    }
}
