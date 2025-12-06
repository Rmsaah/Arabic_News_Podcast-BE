package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressUpdateDto;
import java.util.List;
import java.util.UUID;

/** Service for managing user episode progress and completion tracking */
public interface EpisodeProgressService {

  /**
   * Update user's progress for an episode. User is identified by the requesting username from
   * authentication.
   *
   * @param updateDto Progress update data
   * @param requestingUsername The username of the user making the request
   * @return Updated progress information
   * @throws org.springframework.web.server.ResponseStatusException if unauthorized
   */
  EpisodeProgressDto updateProgress(EpisodeProgressUpdateDto updateDto, String requestingUsername);

  /**
   * Get user's progress for a specific episode. User is identified by the requesting username from
   * authentication.
   *
   * @param episodeId The episode to get progress for
   * @param requestingUsername The username of the user making the request
   * @return Episode progress information
   * @throws org.springframework.web.server.ResponseStatusException if unauthorized
   */
  EpisodeProgressDto getProgress(UUID episodeId, String requestingUsername);

  /**
   * Get all in-progress episodes for a user (for "Continue Listening" feature). User is identified
   * by the requesting username from authentication.
   *
   * @param requestingUsername The username of the user making the request
   * @return List of in-progress episodes with their current positions
   * @throws org.springframework.web.server.ResponseStatusException if unauthorized
   */
  List<EpisodeProgressDto> getInProgressEpisodes(String requestingUsername);

  /**
   * Get analytics data for an episode (average completion, drop-off points). This is an admin-only
   * endpoint.
   */
  EpisodeAnalyticsDto getEpisodeAnalytics(UUID episodeId);

  /**
   * Get user's listening statistics. User is identified by the requesting username from
   * authentication.
   *
   * @param requestingUsername The username of the user making the request
   * @return User's listening statistics
   * @throws org.springframework.web.server.ResponseStatusException if unauthorized
   */
  UserListeningStatsDto getUserListeningStats(String requestingUsername);

  /** DTO for episode analytics */
  record EpisodeAnalyticsDto(
      UUID episodeId,
      String episodeTitle,
      double averageCompletion,
      long totalPlays,
      List<DropOffPoint> commonDropOffPoints) {}

  /** DTO for drop-off analysis */
  record DropOffPoint(int minute, long dropOffCount, String timeFormatted) {}

  /** DTO for user listening statistics */
  record UserListeningStatsDto(
      UUID userId,
      long totalListeningSeconds,
      long completedEpisodes,
      long inProgressEpisodes,
      double averageCompletionRate,
      String formattedTotalTime) {}
}
