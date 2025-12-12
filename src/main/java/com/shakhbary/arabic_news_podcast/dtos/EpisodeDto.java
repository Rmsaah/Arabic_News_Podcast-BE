package com.shakhbary.arabic_news_podcast.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDto {
  private UUID id;
  private String title;
  private String description;
  private String scriptUrlPath;
  private String imageUrl;
  private OffsetDateTime creationDate;

  // Audio reference fields
  private String audioUrlPath;
  private long durationSeconds;

  // Article reference fields
  private UUID articleId;
  private String articleTitle;
  private String articleAuthor; // Author of the source article
  private String articlePublisher; // Publisher of the source article
  private String articleCategory; // Category of the source article

  // Calculated fields
  private double averageRating;
  private int ratingCount;
}
