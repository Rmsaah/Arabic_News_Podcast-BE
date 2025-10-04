package com.shakhbary.arabic_news_podcast.dtos;

import lombok.*;

/**
 * DTO for JSON-based episode import
 * Represents the structure of episode data in JSON files
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeJsonDto {
    // Episode metadata
    private String title;
    private String description;
    private String category;
    private String imgUrl;

    // Content data
    private String transcriptContent;    // Raw text content
    private String audioFilePath;        // Path to audio file

    // Article metadata
    private String author;
    private String publisher;
    private String publishDate;

    // Audio metadata
    private Long durationSeconds;
    private String audioFormat;
    private Long fileSize;

    // Optional identifiers
    private String externalId;          // External system identifier
    private String sourceUrl;           // Original source URL
}
