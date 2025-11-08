package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import java.util.List;

/**
 * Service for automating the complete podcast episode creation workflow
 * from JSON data to playable episodes.
 *
 * This service follows a clean architecture approach where all files
 * (audio, transcripts, images) must be uploaded to cloud storage externally
 * by admins, and only the resulting URLs are provided to this service.
 */
public interface EpisodeAutomationService {

    /**
     * Process a batch of episodes from JSON data
     * @param jsonFilePath Path to JSON file containing episode data
     * @return List of created episodes
     */
    List<EpisodeDto> processEpisodesFromJson(String jsonFilePath);

    /**
     * Process a single episode from JSON object
     * @param episodeJson JSON string containing episode data
     * @return Created episode
     */
    EpisodeDto processEpisodeFromJson(String episodeJson);
}
