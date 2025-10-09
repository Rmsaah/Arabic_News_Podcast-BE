package com.shakhbary.arabic_news_podcast.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;

import java.util.List;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeJsonDto;
import com.shakhbary.arabic_news_podcast.models.Article;
import com.shakhbary.arabic_news_podcast.models.Audio;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.repositories.ArticleRepository;
import com.shakhbary.arabic_news_podcast.repositories.AudioRepository;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.services.EpisodeAutomationService;
import com.shakhbary.arabic_news_podcast.services.CloudStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpisodeAutomationServiceImpl implements EpisodeAutomationService {

    private final EpisodeRepository episodeRepository;
    private final ArticleRepository articleRepository;
    private final AudioRepository audioRepository;
    private final CloudStorageService cloudStorageService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public List<EpisodeDto> processEpisodesFromJson(String jsonFilePath) {
        try {
            // Read JSON file
            List<EpisodeJsonDto> episodeJsonList = objectMapper.readValue(
                    new File(jsonFilePath),
                    new TypeReference<List<EpisodeJsonDto>>() {}
            );

            log.info("Processing {} episodes from JSON file: {}", episodeJsonList.size(), jsonFilePath);

            return episodeJsonList.stream()
                    .map(this::processEpisodeData)
                    .toList();

        } catch (IOException e) {
            log.error("Failed to process JSON file: {}", jsonFilePath, e);
            throw new RuntimeException("Failed to process episodes from JSON", e);
        }
    }

    @Override
    @Transactional
    public EpisodeDto processEpisodeFromJson(String episodeJson) {
        try {
            EpisodeJsonDto episodeJsonDto = objectMapper.readValue(episodeJson, EpisodeJsonDto.class);
            return processEpisodeData(episodeJsonDto);
        } catch (IOException e) {
            log.error("Failed to process JSON string", e);
            throw new RuntimeException("Failed to process episode from JSON", e);
        }
    }

    private EpisodeDto processEpisodeData(EpisodeJsonDto jsonDto) {
        log.info("Processing episode with nested entity structure");

        // Validate required nested objects
        validateJsonDto(jsonDto);

        try {
            // Generate unique ID for file naming if needed
            String uniqueId = UUID.randomUUID().toString();

            // 1. Process Article entity
            Article article = processArticleEntity(jsonDto.getArticle(), uniqueId);
            log.info("Article created with ID: {}", article.getId());

            // 2. Process Audio entity
            Audio audio = processAudioEntity(jsonDto.getAudio(), article, uniqueId);
            log.info("Audio created with ID: {}", audio.getId());

            // 3. Process Episode entity
            Episode episode = processEpisodeEntity(jsonDto.getEpisode(), article, audio);
            log.info("Episode created with ID: {}", episode.getId());

            log.info("Successfully created complete episode: {}", episode.getTitle());

            return convertToDto(episode);

        } catch (Exception e) {
            log.error("Failed to process episode data", e);
            throw new RuntimeException("Failed to process episode: " + e.getMessage(), e);
        }
    }

    /**
     * Validate that all required nested entities are present
     */
    private void validateJsonDto(EpisodeJsonDto jsonDto) {
        if (jsonDto.getArticle() == null) {
            throw new IllegalArgumentException("Article data is required");
        }
        if (jsonDto.getAudio() == null) {
            throw new IllegalArgumentException("Audio data is required");
        }
        if (jsonDto.getEpisode() == null) {
            throw new IllegalArgumentException("Episode data is required");
        }

        // Validate Article fields
        EpisodeJsonDto.ArticleData article = jsonDto.getArticle();
        if (article.getTitle() == null || article.getTitle().isBlank()) {
            throw new IllegalArgumentException("Article title is required");
        }

        // Validate Audio fields
        EpisodeJsonDto.AudioData audio = jsonDto.getAudio();
        if (audio.getUrlPath() == null && audio.getAudioFilePath() == null) {
            throw new IllegalArgumentException("Either audio urlPath or audioFilePath is required");
        }

        // Validate Episode fields
        EpisodeJsonDto.EpisodeData episode = jsonDto.getEpisode();
        if (episode.getTitle() == null || episode.getTitle().isBlank()) {
            throw new IllegalArgumentException("Episode title is required");
        }
    }

    /**
     * Process and create Article entity from JSON data
     */
    private Article processArticleEntity(EpisodeJsonDto.ArticleData articleData, String uniqueId) {
        try {
            Article article = new Article();

            // Set basic fields
            article.setTitle(articleData.getTitle());
            article.setCategory(articleData.getCategory());
            article.setAuthor(articleData.getAuthor());
            article.setPublisher(articleData.getPublisher());

            // Parse published date if provided
            if (articleData.getPublishedAt() != null && !articleData.getPublishedAt().isBlank()) {
                article.setPublishedAt(OffsetDateTime.parse(articleData.getPublishedAt()));
            }

            // Set content
            article.setContentRaw(articleData.getContentRaw());
            article.setContentCleaned(articleData.getContentCleaned() != null
                    ? articleData.getContentCleaned()
                    : articleData.getContentRaw());

            // Handle URL path - use provided or upload content
            String urlPath = articleData.getUrlPath();
            if (urlPath == null || urlPath.isBlank()) {
                // If no URL provided and we have content, upload it
                if (articleData.getContentRaw() != null && !articleData.getContentRaw().isBlank()) {
                    urlPath = processTranscript(articleData.getContentRaw(), uniqueId);
                }
            }
            article.setUrlPath(urlPath);

            // Set fetch timestamp
            article.setFetchedAt(OffsetDateTime.now());

            return articleRepository.save(article);

        } catch (Exception e) {
            log.error("Failed to process Article entity", e);
            throw new RuntimeException("Failed to create Article", e);
        }
    }

    /**
     * Process and create Audio entity from JSON data
     */
    private Audio processAudioEntity(EpisodeJsonDto.AudioData audioData, Article article, String uniqueId) {
        try {
            Audio audio = new Audio();

            // Link to article
            audio.setArticle(article);

            // Set audio metadata
            audio.setDuration(audioData.getDuration() != null ? audioData.getDuration() : 0L);
            audio.setFormat(audioData.getFormat());

            // Handle URL path - use provided or upload file
            String urlPath = audioData.getUrlPath();
            if (urlPath == null || urlPath.isBlank()) {
                // If no URL provided but we have a file path, upload it
                if (audioData.getAudioFilePath() != null && !audioData.getAudioFilePath().isBlank()) {
                    urlPath = processAudioFile(audioData.getAudioFilePath(), uniqueId);
                } else {
                    throw new IllegalArgumentException("Audio must have either urlPath or audioFilePath");
                }
            }
            audio.setUrlPath(urlPath);

            // Set creation timestamp
            audio.setCreatedAt(OffsetDateTime.now());

            return audioRepository.save(audio);

        } catch (Exception e) {
            log.error("Failed to process Audio entity", e);
            throw new RuntimeException("Failed to create Audio", e);
        }
    }

    /**
     * Process and create Episode entity from JSON data
     */
    private Episode processEpisodeEntity(EpisodeJsonDto.EpisodeData episodeData, Article article, Audio audio) {
        try {
            Episode episode = new Episode();

            // Link to article and audio
            episode.setArticle(article);
            episode.setAudio(audio);

            // Set episode fields
            episode.setTitle(episodeData.getTitle());
            episode.setDescription(episodeData.getDescription());
            episode.setImageUrl(episodeData.getImgUrl());

            // Set transcript URL - use provided or fall back to article URL
            String transcriptUrl = episodeData.getTranscriptUrlPath();
            if (transcriptUrl == null || transcriptUrl.isBlank()) {
                transcriptUrl = article.getUrlPath();
            }
            episode.setTranscript(transcriptUrl);

            // Set creation timestamp
            episode.setCreatedAt(OffsetDateTime.now());

            return episodeRepository.save(episode);

        } catch (Exception e) {
            log.error("Failed to process Episode entity", e);
            throw new RuntimeException("Failed to create Episode", e);
        }
    }

    @Override
    public String processTranscript(String transcriptContent, String episodeId) {
        try {
            // Create temporary file for transcript
            String fileName = String.format("transcripts/%s-transcript.txt", episodeId);
            String tempFilePath = createTempFile(transcriptContent, "transcript", ".txt");

            // Upload to cloud storage
            String publicUrl = cloudStorageService.uploadFile(tempFilePath, fileName, "text/plain");

            // Cleanup temp file
            Files.deleteIfExists(Paths.get(tempFilePath));

            log.info("Uploaded transcript for episode {}: {}", episodeId, publicUrl);
            return publicUrl;

        } catch (Exception e) {
            log.error("Failed to process transcript for episode: {}", episodeId, e);
            throw new RuntimeException("Failed to process transcript", e);
        }
    }

    @Override
    public String processAudioFile(String audioFilePath, String episodeId) {
        try {
            // Determine file extension
            String extension = audioFilePath.substring(audioFilePath.lastIndexOf('.'));
            String fileName = String.format("audio/%s-audio%s", episodeId, extension);

            // Upload to cloud storage
            String mimeType = getMimeType(extension);
            String publicUrl = cloudStorageService.uploadFile(audioFilePath, fileName, mimeType);

            log.info("Uploaded audio for episode {}: {}", episodeId, publicUrl);
            return publicUrl;

        } catch (Exception e) {
            log.error("Failed to process audio file for episode: {}", episodeId, e);
            throw new RuntimeException("Failed to process audio file", e);
        }
    }

    @Override
    public String uploadToCloudStorage(String localFilePath, String fileName) {
        return cloudStorageService.uploadFile(localFilePath, fileName, "application/octet-stream");
    }

    /**
     * Convert Episode entity to DTO for API response
     */
    private EpisodeDto convertToDto(Episode episode) {
        return new EpisodeDto(
                episode.getId(),
                episode.getTitle(),
                episode.getDescription(),
                episode.getTranscript(),
                episode.getAudio() != null ? episode.getAudio().getUrlPath() : null,
                episode.getAudio() != null ? episode.getAudio().getDuration() : 0L,
                0.0, // No ratings yet
                0,   // No ratings yet
                episode.getCreatedAt(),
                episode.getArticle() != null ? episode.getArticle().getId() : null,
                episode.getArticle() != null ? episode.getArticle().getTitle() : null,
                episode.getImageUrl()
        );
    }

    /**
     * Create temporary file for upload operations
     */
    private String createTempFile(String content, String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        Files.write(tempFile.toPath(), content.getBytes());
        return tempFile.getAbsolutePath();
    }

    /**
     * Determine MIME type from file extension
     */
    private String getMimeType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".mp3" -> "audio/mpeg";
            case ".wav" -> "audio/wav";
            case ".ogg" -> "audio/ogg";
            case ".m4a" -> "audio/mp4";
            case ".flac" -> "audio/flac";
            default -> "audio/mpeg";
        };
    }
}