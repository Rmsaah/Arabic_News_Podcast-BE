package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.CreateSampleDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import java.util.List;

/**
 * Service for automating the complete podcast episode creation workflow from JSON data to playable
 * episodes.
 *
 * <p>This service follows a clean architecture approach where all files (audio, transcripts,
 * images) must be uploaded to cloud storage externally by admins, and only the resulting URLs are
 * provided to this service.
 */
public interface EpisodeAutomationService {

  /**
   * Process a batch of episodes from POJO object
   *
   * @param createSampleDtoList POJO list that holds the episode data
   * @return List of created episodes
   */
  List<EpisodeDto> createBulkEpisodes(List<CreateSampleDto> createSampleDtoList);

  /**
   * Process a single episode from POJO object
   *
   * @param createSampleDto POJO that holds the episode data
   * @return Created episode
   */
  EpisodeDto createEpisode(CreateSampleDto createSampleDto);
}
