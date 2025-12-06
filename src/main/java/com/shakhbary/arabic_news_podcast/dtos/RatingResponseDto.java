package com.shakhbary.arabic_news_podcast.dtos;

import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseDto {
  private UUID ratingId;
  private UUID userId;
  private UUID episodeId;
  private int rating;
  private String message;
}
