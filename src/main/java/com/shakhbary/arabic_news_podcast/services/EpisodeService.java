package com.shakhbary.arabic_news_podcast.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeCreateRequestDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;

/**
 * Service for managing podcast episodes
 */
public interface EpisodeService {

    /**
     * Retrieve a paginated list of all episodes
     * @param pageable Pagination parameters (page number, size, sorting)
     * @return Page containing episode DTOs and pagination metadata
     */
    Page<EpisodeDto> listEpisodes(Pageable pageable);

    /**
     * Retrieve a single episode by its unique identifier
     * @param episodeId The unique identifier of the episode
     * @return EpisodeDto containing episode details
     */
    EpisodeDto getEpisode(UUID episodeId);

    /**
     * Create a new episode from article and audio data
     * @param request DTO containing episode creation data (article, audio, metadata)
     * @return Created EpisodeDto with generated ID and timestamps
     */
    EpisodeDto createEpisode(EpisodeCreateRequestDto request);

    /**
     * Retrieve the most recently created episodes (for daily digest feature)
     * @param limit Maximum number of episodes to return
     * @return List of recent episode DTOs ordered by creation date
     */
    java.util.List<EpisodeDto> listDailyEpisodes(int limit);

    /**
     * Search for episodes by title and/or category with pagination
     * @param title Search term for episode title (optional, can be null)
     * @param category Filter by category (optional, can be null)
     * @param pageable Pagination parameters
     * @return Page containing matching episode DTOs
     */
    Page<EpisodeDto> searchEpisodes(String title, String category, Pageable pageable);
}
