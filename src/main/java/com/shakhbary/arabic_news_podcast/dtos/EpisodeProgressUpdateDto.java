package com.shakhbary.arabic_news_podcast.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeProgressUpdateDto {
  @NotNull(message = "Episode ID is required")
  private UUID episodeId;

  @Min(value = 0, message = "Position must be non-negative")
  private long positionSeconds; // Exact second where user is/stopped

  private boolean isCompleted = false; // Optional: explicitly mark as completed
}
