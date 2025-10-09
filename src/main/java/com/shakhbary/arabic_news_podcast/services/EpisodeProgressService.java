package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing user episode progress and completion tracking
 */
public interface EpisodeProgressService {

    /**
     * Update user's progress for an episode
     */
    EpisodeProgressDto updateProgress(UUID userId, EpisodeProgressUpdateDto updateDto);

    /**
     * Get user's progress for a specific episode
     */
    EpisodeProgressDto getProgress(UUID userId, UUID episodeId);

    /**
     * Get all in-progress episodes for a user (for "Continue Listening" feature)
     */
    List<EpisodeProgressDto> getInProgressEpisodes(UUID userId);

    /**
     * Get completed episodes for a user with pagination
     */
    Page<EpisodeProgressDto> getCompletedEpisodes(UUID userId, Pageable pageable);

    /**
     * Get analytics data for an episode (average completion, drop-off points)
     */
    EpisodeAnalyticsDto getEpisodeAnalytics(UUID episodeId);

    /**
     * Get user's listening statistics
     */
    UserListeningStatsDto getUserListeningStats(UUID userId);

    /**
     * DTO for episode analytics
     */
    record EpisodeAnalyticsDto(
            UUID episodeId,
            String episodeTitle,
            double averageCompletion,
            long totalPlays,
            List<DropOffPoint> commonDropOffPoints
    ) {}

    /**
     * DTO for drop-off analysis
     */
    record DropOffPoint(
            int minute,
            long dropOffCount,
            String timeFormatted
    ) {}

    /**
     * DTO for user listening statistics
     */
    record UserListeningStatsDto(
            UUID userId,
            long totalListeningSeconds,
            long completedEpisodes,
            long inProgressEpisodes,
            double averageCompletionRate,
            String formattedTotalTime
    ) {}
}
