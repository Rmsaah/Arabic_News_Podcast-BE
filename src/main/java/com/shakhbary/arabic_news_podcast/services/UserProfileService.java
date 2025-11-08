package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.UserProfileDto;

import java.util.UUID;

/**
 * Service for managing user profile and listening activity
 */
public interface UserProfileService {

    /**
     * Retrieve complete user profile with listening statistics.
     * Validates that the requesting user has permission to view this profile.
     *
     * @param userId The unique identifier of the user
     * @param requestingUsername The username of the user making the request
     * @return UserProfileDto containing profile data and listening stats
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    UserProfileDto getUserProfile(UUID userId, String requestingUsername);

    /**
     * Track and accumulate user's total listening time.
     * Validates that the requesting user has permission to update this user's data.
     *
     * @param userId The unique identifier of the user
     * @param secondsListened Number of seconds to add to user's total listening time
     * @param requestingUsername The username of the user making the request
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    void trackListeningTime(UUID userId, long secondsListened, String requestingUsername);

    /**
     * Update user's playback position for an episode.
     * Validates that the requesting user has permission to update this user's data.
     *
     * @param userId The unique identifier of the user
     * @param episodeId The unique identifier of the episode
     * @param positionSeconds Current playback position in seconds
     * @param requestingUsername The username of the user making the request
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    void updateEpisodeProgress(UUID userId, UUID episodeId, long positionSeconds, String requestingUsername);

    /**
     * Mark an episode as completed by the user.
     * Validates that the requesting user has permission to update this user's data.
     *
     * @param userId The unique identifier of the user
     * @param episodeId The unique identifier of the episode
     * @param positionSeconds Final playback position in seconds (usually total duration)
     * @param requestingUsername The username of the user making the request
     * @throws org.springframework.web.server.ResponseStatusException if unauthorized
     */
    void markEpisodeCompleted(UUID userId, UUID episodeId, long positionSeconds, String requestingUsername);
}
