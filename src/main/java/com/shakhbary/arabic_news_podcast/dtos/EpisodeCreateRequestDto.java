package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeCreateRequestDto {
    // Core Episode fields
    @NotBlank(message = "Title is required")
    @Size(max = 250)
    private String title;

    @Size(max = 2000)
    private String description;

    @Size(max = 500)
    private String imgUrl;

    // References to existing related resources OR triggers to create them
    private UUID articleId;            // if existing article is already created

    @Size(max = 1000)
    private String transcriptUrlPath;  // remote path to transcript text (LLM output)

    @Size(max = 1000)
    private String audioUrlPath;       // remote path to audio file (TTS output)

    // Optional hints/metadata (if audio entity needs to be created)
    @Positive(message = "Duration must be positive")
    private Long durationSeconds;      // can be null; backend may probe metadata

    @Size(max = 20)
    private String audioFormat;        // e.g., mp3, wav
}
