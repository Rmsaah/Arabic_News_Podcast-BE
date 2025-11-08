package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;

/**
 * DTO for JSON-based episode import with nested entity structure.
 * This represents a complete episode with explicit Article, Audio, and Episode data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeJsonDto {

    /**
     * Article entity data
     */
    private ArticleData article;

    /**
     * Audio entity data
     */
    private AudioData audio;

    /**
     * Episode entity data
     */
    private EpisodeData episode;

    /**
     * Nested DTO for Article entity fields
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleData {
        private String title;
        private String category;
        private String author;
        private String publisher;
        private String publishedAt;           // ISO date string
        private String contentRawUrl;         // URL to raw text content in cloud storage
        private String scriptUrl;             // URL to processed/cleaned script in cloud storage
    }

    /**
     * Nested DTO for Audio entity fields
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioData {
        private Long duration;                // Duration in seconds
        private String format;                // e.g., "mp3", "wav", "m4a"
        private String urlPath;               // URL to audio file in cloud storage (required)
    }

    /**
     * Nested DTO for Episode entity fields
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpisodeData {
        private String title;
        private String description;
        private String scriptUrlPath;     // // URL to script (optional, uses article.scriptUrl if not provided)
        private String imageUrl;                // Episode thumbnail/cover image URL
    }
}