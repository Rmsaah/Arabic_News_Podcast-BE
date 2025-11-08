package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressUpdateDto;
import com.shakhbary.arabic_news_podcast.services.EpisodeProgressService;
import com.shakhbary.arabic_news_podcast.services.EpisodeProgressService.EpisodeAnalyticsDto;
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
     *
     * @param userId User ID
     * @param updateDto Progress update data (episode ID, position, etc.)
     * @return Updated progress information
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public EpisodeProgressDto updateProgress(
            @RequestParam UUID userId,
            @RequestBody @Valid EpisodeProgressUpdateDto updateDto) {

        return episodeProgressService.updateProgress(userId, updateDto);
    }

    /**
     * Get progress for a specific episode and user.
     * Returns the saved playback position and completion status.
     *
     * @param episodeId Episode ID
     * @param userId User ID
     * @return Episode progress information
     */
    @GetMapping("/episodes/{episodeId}")
    public EpisodeProgressDto getEpisodeProgress(
            @PathVariable UUID episodeId,
            @RequestParam UUID userId) {

        return episodeProgressService.getProgress(userId, episodeId);
    }

    /**
     * Get all in-progress episodes for a user (Continue Listening feature).
     * Returns episodes that the user has started but not completed.
     *
     * @param userId User ID
     * @return List of in-progress episodes with their current positions
     */
    @GetMapping("/users/{userId}/in-progress")
    public List<EpisodeProgressDto> getInProgressEpisodes(@PathVariable UUID userId) {
        return episodeProgressService.getInProgressEpisodes(userId);
    }

    /**
     * Get user's listening statistics.
     * Returns total listening time, episodes completed, etc.
     *
     * @param userId User ID
     * @return User's listening statistics
     */
    @GetMapping("/users/{userId}/stats")
    public UserListeningStatsDto getUserStats(@PathVariable UUID userId) {
        return episodeProgressService.getUserListeningStats(userId);
    }

    /**
     * Get analytics for an episode (admin/content creator endpoint).
     * Returns statistics like total listens, completion rate, average progress, etc.
     *
     * @param episodeId Episode ID
     * @return Episode analytics data
     */
    @GetMapping("/episodes/{episodeId}/analytics")
    public EpisodeAnalyticsDto getEpisodeAnalytics(@PathVariable UUID episodeId) {
        return episodeProgressService.getEpisodeAnalytics(episodeId);
    }

    // ==========================================
    // Playback Tracking Endpoints
    // ==========================================

    /**
     * Track listening time for a user.
     * Call this periodically while the episode is playing, or when the user pauses/stops.
     * This updates the total listening time for the user's profile.
     *
     * @param episodeId Episode ID (for context/logging)
     * @param userId User ID
     * @param secondsListened Number of seconds listened in this session
     * @param authentication Current authenticated user
     */
    @PostMapping("/episodes/{episodeId}/track-listening")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trackListeningTime(
            @PathVariable UUID episodeId,
            @RequestParam UUID userId,
            @RequestParam long secondsListened,
            Authentication authentication) {

        userProfileService.trackListeningTime(userId, secondsListened, authentication.getName());
    }

    /**
     * Update episode progress with exact position (for resume functionality).
     * Use this to save where the user is in the episode for later resumption.
     *
     * @param episodeId Episode ID
     * @param userId User ID
     * @param positionSeconds Current position in seconds
     * @param authentication Current authenticated user
     */
    @PostMapping("/episodes/{episodeId}/position")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEpisodePosition(
            @PathVariable UUID episodeId,
            @RequestParam UUID userId,
            @RequestParam long positionSeconds,
            Authentication authentication) {

        userProfileService.updateEpisodeProgress(userId, episodeId, positionSeconds, authentication.getName());
    }

    /**
     * Mark an episode as completed.
     * Call this when the user reaches the end or explicitly marks it as finished.
     *
     * @param episodeId Episode ID
     * @param userId User ID
     * @param positionSeconds Final position in seconds (typically the episode duration)
     * @param authentication Current authenticated user
     */
    @PostMapping("/episodes/{episodeId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markEpisodeCompleted(
            @PathVariable UUID episodeId,
            @RequestParam UUID userId,
            @RequestParam long positionSeconds,
            Authentication authentication) {

        userProfileService.markEpisodeCompleted(userId, episodeId, positionSeconds, authentication.getName());
    }
}
