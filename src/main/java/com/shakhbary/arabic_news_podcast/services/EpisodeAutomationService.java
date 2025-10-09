package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import java.util.List;


/**
 * Service for automating the complete podcast episode creation workflow
 * from JSON data to playable episodes
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

    /**
     * Upload files to cloud storage and make them publicly accessible
     * @param localFilePath Path to local file
     * @param fileName Desired filename in cloud storage
     * @return Public URL of uploaded file
     */
    String uploadToCloudStorage(String localFilePath, String fileName);

    /**
     * Process transcript text and generate public URL
     * @param transcriptContent Text content
     * @param episodeId Episode identifier for filename
     * @return Public URL to transcript
     */
    String processTranscript(String transcriptContent, String episodeId);

    /**
     * Process audio file and generate public URL
     * @param audioFilePath Path to audio file
     * @param episodeId Episode identifier for filename
     * @return Public URL to audio file
     */
    String processAudioFile(String audioFilePath, String episodeId);
}
