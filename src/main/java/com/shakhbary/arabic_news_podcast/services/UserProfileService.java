package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.UserProfileDto;

import java.util.UUID;

/**
 * Service for managing user profile and listening activity
 */
public interface UserProfileService {

    /**
     * Retrieve complete user profile with listening statistics
     * @param userId The unique identifier of the user
     * @return UserProfileDto containing profile data and listening stats
     */
    UserProfileDto getUserProfile(UUID userId);

    /**
     * Track and accumulate user's total listening time
     * @param userId The unique identifier of the user
     * @param secondsListened Number of seconds to add to user's total listening time
     */
    void trackListeningTime(UUID userId, long secondsListened);

    /**
     * Update user's playback position for an episode
     * @param userId The unique identifier of the user
     * @param episodeId The unique identifier of the episode
     * @param positionSeconds Current playback position in seconds
     */
    void updateEpisodeProgress(UUID userId, UUID episodeId, long positionSeconds);

    /**
     * Mark an episode as completed by the user
     * @param userId The unique identifier of the user
     * @param episodeId The unique identifier of the episode
     * @param positionSeconds Final playback position in seconds (usually total duration)
     */
    void markEpisodeCompleted(UUID userId, UUID episodeId, long positionSeconds);
}
