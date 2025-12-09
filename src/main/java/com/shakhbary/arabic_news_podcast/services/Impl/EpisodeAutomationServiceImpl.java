package com.shakhbary.arabic_news_podcast.services.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeJsonDto;
import com.shakhbary.arabic_news_podcast.models.Article;
import com.shakhbary.arabic_news_podcast.models.Audio;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.repositories.ArticleRepository;
import com.shakhbary.arabic_news_podcast.repositories.AudioRepository;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.services.EpisodeAutomationService;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpisodeAutomationServiceImpl implements EpisodeAutomationService {

  private final EpisodeRepository episodeRepository;
  private final ArticleRepository articleRepository;
  private final AudioRepository audioRepository;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate = new RestTemplate();

  private static final String AGENT_BASE_URL = "http://python_service:8001/api";

  /**
   * Automated pipeline: Scrape news → Process all → Save to database Calls Python agent to do all
   * the heavy lifting
   */
  @Transactional
  @Scheduled(cron = "0 0 9 * * *") // CRON expression for 9:00 AM daily
  public List<EpisodeDto> automatedDailyPipeline() {
    try {
      log.info("Starting automated daily pipeline...");

      // Call Python agent
      String url = AGENT_BASE_URL + "/scrape-and-process-all";

      log.info("Calling Python agent: {}", url);
      ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        Map<String, Object> body = response.getBody();
        Boolean success = (Boolean) body.get("success");

        if (Boolean.TRUE.equals(success)) {
          List<Map<String, Object>> episodesData = (List<Map<String, Object>>) body.get("episodes");

          log.info("✓ Received {} episodes from agent", episodesData.size());

          List<EpisodeDto> savedEpisodes = new ArrayList<>();

          // Process each episode
          for (Map<String, Object> episodeData : episodesData) {
            try {
              String episodeJson = objectMapper.writeValueAsString(episodeData);
              EpisodeDto dto = processEpisodeFromJson(episodeJson);
              savedEpisodes.add(dto);
              log.info("Saved episode: {}", dto.getTitle());
            } catch (Exception e) {
              log.error("Error saving episode: {}", e.getMessage());
            }
          }

          log.info(
              "Pipeline complete: {}/{} episodes saved", savedEpisodes.size(), episodesData.size());

          return savedEpisodes;
        } else {
          log.error("Agent returned failure");
          throw new RuntimeException("Agent processing failed");
        }
      } else {
        log.error("Agent returned non-200 status");
        throw new RuntimeException("Agent not responding");
      }

    } catch (Exception e) {
      log.error("Error in automated pipeline: {}", e.getMessage());
      throw new RuntimeException("Automated pipeline failed", e);
    }
  }

  @Override
  @Transactional
  public List<EpisodeDto> processEpisodesFromJson(String jsonFilePath) {
    try {
      List<EpisodeJsonDto> episodeJsonList =
          objectMapper.readValue(
              new File(jsonFilePath), new TypeReference<List<EpisodeJsonDto>>() {});

      log.info("Processing {} episodes from JSON file: {}", episodeJsonList.size(), jsonFilePath);

      return episodeJsonList.stream().map(this::processEpisodeData).toList();

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

    validateJsonDto(jsonDto);

    try {
      Article article = processArticleEntity(jsonDto.getArticle());
      log.info("Article created with ID: {}", article.getId());

      Audio audio = processAudioEntity(jsonDto.getAudio(), article);
      log.info("Audio created with ID: {}", audio.getId());

      Episode episode = processEpisodeEntity(jsonDto.getEpisode(), article, audio);
      log.info("Episode created with ID: {}", episode.getId());

      log.info("Successfully created complete episode: {}", episode.getTitle());

      return convertToDto(episode);

    } catch (Exception e) {
      log.error("Failed to process episode data", e);
      throw new RuntimeException("Failed to process episode: " + e.getMessage(), e);
    }
  }

  /** Validate that all required nested entities are present */
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
    if (audio.getUrlPath() == null || audio.getUrlPath().isBlank()) {
      throw new IllegalArgumentException("Audio urlPath is required");
    }

    // Validate Episode fields
    EpisodeJsonDto.EpisodeData episode = jsonDto.getEpisode();
    if (episode.getTitle() == null || episode.getTitle().isBlank()) {
      throw new IllegalArgumentException("Episode title is required");
    }
  }

  /** Process and create Article entity from JSON data */
  private Article processArticleEntity(EpisodeJsonDto.ArticleData articleData) {
    try {
      Article article = new Article();

      // Set basic fields
      article.setTitle(articleData.getTitle());
      article.setCategory(articleData.getCategory());
      article.setAuthor(articleData.getAuthor());
      article.setPublisher(articleData.getPublisher());

      // Parse published date if provided
      if (articleData.getPublicationDate() != null && !articleData.getPublicationDate().isBlank()) {
        article.setPublicationDate(OffsetDateTime.parse(articleData.getPublicationDate()));
      }

      // Set content URLs (must be provided as cloud storage URLs)
      article.setContentRawUrl(articleData.getContentRawUrl());
      article.setScriptUrl(
          articleData.getScriptUrl() != null
              ? articleData.getScriptUrl()
              : articleData.getContentRawUrl());

      // Set fetch timestamp
      article.setFetchDate(OffsetDateTime.now());

      return articleRepository.save(article);

    } catch (Exception e) {
      log.error("Failed to process Article entity", e);
      throw new RuntimeException("Failed to create Article", e);
    }
  }

  /** Process and create Audio entity from JSON data */
  private Audio processAudioEntity(EpisodeJsonDto.AudioData audioData, Article article) {
    try {
      Audio audio = new Audio();

      // Link to article
      audio.setArticle(article);

      // Set audio metadata
      audio.setDuration(audioData.getDuration() != null ? audioData.getDuration() : 0L);
      audio.setFormat(audioData.getFormat());

      // Require URL path (must be provided as cloud storage URL)
      String urlPath = audioData.getUrlPath();
      if (urlPath == null || urlPath.isBlank()) {
        throw new IllegalArgumentException("Audio urlPath is required");
      }
      audio.setUrlPath(urlPath);

      // Set creation timestamp
      audio.setCreationDate(OffsetDateTime.now());

      return audioRepository.save(audio);

    } catch (Exception e) {
      log.error("Failed to process Audio entity", e);
      throw new RuntimeException("Failed to create Audio", e);
    }
  }

  /** Process and create Episode entity from JSON data */
  private Episode processEpisodeEntity(
      EpisodeJsonDto.EpisodeData episodeData, Article article, Audio audio) {
    try {
      Episode episode = new Episode();

      // Link to article and audio
      episode.setArticle(article);
      episode.setAudio(audio);

      // Set episode fields
      episode.setTitle(episodeData.getTitle());
      episode.setDescription(episodeData.getDescription());
      episode.setImageUrl(episodeData.getImageUrl());

      // Set script URL - use provided or fall back to article script URL
      String scriptUrl = episodeData.getScriptUrlPath();
      if (scriptUrl == null || scriptUrl.isBlank()) {
        scriptUrl = article.getScriptUrl();
      }
      episode.setScriptUrlPath(scriptUrl);

      // Set creation timestamp
      episode.setCreationDate(OffsetDateTime.now());

      return episodeRepository.save(episode);

    } catch (Exception e) {
      log.error("Failed to process Episode entity", e);
      throw new RuntimeException("Failed to create Episode", e);
    }
  }

  /*
   * Convert Episode entity to DTO for API response
   */
  private EpisodeDto convertToDto(Episode episode) {
    return new EpisodeDto(
        episode.getId(),
        episode.getTitle(),
        episode.getDescription(),
        episode.getScriptUrlPath(),
        episode.getAudio() != null ? episode.getAudio().getUrlPath() : null,
        episode.getAudio() != null ? episode.getAudio().getDuration() : 0L,
        0.0, // No ratings yet
        0, // No ratings yet
        episode.getCreationDate(),
        episode.getArticle() != null ? episode.getArticle().getId() : null,
        episode.getArticle() != null ? episode.getArticle().getTitle() : null,
        episode.getArticle() != null ? episode.getArticle().getAuthor() : null,
        episode.getArticle() != null ? episode.getArticle().getPublisher() : null,
        episode.getArticle() != null ? episode.getArticle().getCategory() : null,
        episode.getImageUrl());
  }
}
