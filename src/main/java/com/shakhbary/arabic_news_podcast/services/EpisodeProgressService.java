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
     * Update user's progress for an episode.
     * Validates that the requesting user has permission to update this user's progress.
     *
     * @param userId The user whose progress to update
     * @param updateDto Progress update data
     * @param requestingUsername The username of the user making the request
     * @return Updated progress information
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    EpisodeProgressDto updateProgress(UUID userId, EpisodeProgressUpdateDto updateDto, String requestingUsername);

    /**
     * Get user's progress for a specific episode.
     * Validates that the requesting user has permission to view this user's progress.
     *
     * @param userId The user whose progress to retrieve
     * @param episodeId The episode to get progress for
     * @param requestingUsername The username of the user making the request
     * @return Episode progress information
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    EpisodeProgressDto getProgress(UUID userId, UUID episodeId, String requestingUsername);

    /**
     * Get all in-progress episodes for a user (for "Continue Listening" feature).
     * Validates that the requesting user has permission to view this user's data.
     *
     * @param userId The user whose in-progress episodes to retrieve
     * @param requestingUsername The username of the user making the request
     * @return List of in-progress episodes with their current positions
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    List<EpisodeProgressDto> getInProgressEpisodes(UUID userId, String requestingUsername);

    /**
     * Get completed episodes for a user with pagination
     */
    Page<EpisodeProgressDto> getCompletedEpisodes(UUID userId, Pageable pageable);

    /**
     * Get analytics data for an episode (average completion, drop-off points).
     * This is an admin-only endpoint.
     */
    EpisodeAnalyticsDto getEpisodeAnalytics(UUID episodeId);

    /**
     * Get user's listening statistics.
     * Validates that the requesting user has permission to view this user's stats.
     *
     * @param userId The user whose stats to retrieve
     * @param requestingUsername The username of the user making the request
     * @return User's listening statistics
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    UserListeningStatsDto getUserListeningStats(UUID userId, String requestingUsername);

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
