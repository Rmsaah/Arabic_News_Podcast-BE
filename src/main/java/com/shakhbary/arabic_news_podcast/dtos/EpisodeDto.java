package com.shakhbary.arabic_news_podcast.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Article reference fields
    private UUID articleId;
    private String articleTitle;
    private String articleAuthor;      // Author of the source article
    private String articlePublisher;   // Publisher of the source article
    private String articleCategory;    // Category of the source article

    private String imageUrl;

    /**
     * Simplified constructor for creation response.
     * Returns only the ID and creation timestamp after successfully creating an episode.
     */
    public EpisodeDto(UUID id, OffsetDateTime creationDate) {
        this.id = id;
        this.creationDate = creationDate;
    }

    public EpisodeDto(UUID id2, String title2, String description2, String scriptUrlPath2, Object object, long l,
            double d, int count, OffsetDateTime creationDate2, Object object2, Object object3, String imageUrl2) {
        //TODO Auto-generated constructor stub
    }
}
