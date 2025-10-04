package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeHistoryDto {
    private UUID episodeId;
    private String episodeTitle;
    private String episodeImageUrl;
    private OffsetDateTime completedAt;
    private int rating; // 0 if not rated, 1-5 if rated
}
