package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressUpdateDto;
import com.shakhbary.arabic_news_podcast.services.EpisodeProgressService;
import com.shakhbary.arabic_news_podcast.services.EpisodeProgressService.UserListeningStatsDto;
import com.shakhbary.arabic_news_podcast.services.UserProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing episode playback progress and listening statistics.
 * Handles progress tracking, completion status, and analytics for both users and episodes.
 * <p>
 * This controller consolidates all progress-related functionality including:
 * <ul>
 *   <li>Progress tracking (where user stopped/paused)</li>
 *   <li>Listening time tracking</li>
 *   <li>Episode completion marking</li>
 *   <li>User statistics and analytics</li>
 *   <li>In-progress episodes (Continue Listening feature)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class EpisodeProgressController {

    private final EpisodeProgressService episodeProgressService;
    private final UserProfileService userProfileService;

    /**
     * Update episode progress (position where user stopped/paused).
     * Use this endpoint to save the user's current position in an episode.
     * User is determined from authentication token.
     *
     * @param updateDto Progress update data (episode ID, position, etc.)
     * @param authentication Current authenticated user
     * @return Updated progress information
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public EpisodeProgressDto updateProgress(
            @RequestBody @Valid EpisodeProgressUpdateDto updateDto,
            Authentication authentication) {

        return episodeProgressService.updateProgress(updateDto, authentication.getName());
    }

    /**
     * Get progress for a specific episode and user.
     * Returns the saved playback position and completion status.
     * User is determined from authentication token.
     *
     * @param episodeId Episode ID
     * @param authentication Current authenticated user
     * @return Episode progress information
     */
    @GetMapping("/episodes/{episodeId}")
    public EpisodeProgressDto getEpisodeProgress(
            @PathVariable UUID episodeId,
            Authentication authentication) {

        return episodeProgressService.getProgress(episodeId, authentication.getName());
    }

    /**
     * Get all in-progress episodes for a user (Continue Listening feature).
     * Returns episodes that the user has started but not completed.
     * User is determined from authentication token.
     *
     * @param authentication Current authenticated user
     * @return List of in-progress episodes with their current positions
     */
    @GetMapping("/in-progress")
    public List<EpisodeProgressDto> getInProgressEpisodes(Authentication authentication) {
        return episodeProgressService.getInProgressEpisodes(authentication.getName());
    }

    /**
     * Get user's listening statistics.
     * Returns total listening time, episodes completed, etc.
     * User is determined from authentication token.
     *
     * @param authentication Current authenticated user
     * @return User's listening statistics
     */
    @GetMapping("/stats")
    public UserListeningStatsDto getUserStats(Authentication authentication) {
        return episodeProgressService.getUserListeningStats(authentication.getName());
    }

    // ==========================================
    // Playback Tracking Endpoints
    // ==========================================

    /**
     * Track listening time for a user.
     * Call this periodically while the episode is playing, or when the user pauses/stops.
     * This updates the total listening time for the user's profile.
     * User is determined from authentication token.
     *
     * @param episodeId Episode ID (for context/logging)
     * @param secondsListened Number of seconds listened in this session
     * @param authentication Current authenticated user
     */
    @PostMapping("/episodes/{episodeId}/track-listening")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trackListeningTime(
            @PathVariable UUID episodeId,
            @RequestParam long secondsListened,
            Authentication authentication) {

        userProfileService.trackListeningTime(secondsListened, authentication.getName());
    }

    /**
     * Update episode progress with exact position (for resume functionality).
     * Use this to save where the user is in the episode for later resumption.
     * User is determined from authentication token.
     *
     * @param episodeId Episode ID
     * @param positionSeconds Current position in seconds
     * @param authentication Current authenticated user
     */
    @PostMapping("/episodes/{episodeId}/position")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEpisodePosition(
            @PathVariable UUID episodeId,
            @RequestParam long positionSeconds,
            Authentication authentication) {

        userProfileService.updateEpisodeProgress(episodeId, positionSeconds, authentication.getName());
    }

    /**
     * Mark an episode as completed.
     * Call this when the user reaches the end or explicitly marks it as finished.
     * User is determined from authentication token.
     *
     * @param episodeId Episode ID
     * @param positionSeconds Final position in seconds (typically the episode duration)
     * @param authentication Current authenticated user
     */
    @PostMapping("/episodes/{episodeId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markEpisodeCompleted(
            @PathVariable UUID episodeId,
            @RequestParam long positionSeconds,
            Authentication authentication) {

        userProfileService.markEpisodeCompleted(episodeId, positionSeconds, authentication.getName());
    }
}
