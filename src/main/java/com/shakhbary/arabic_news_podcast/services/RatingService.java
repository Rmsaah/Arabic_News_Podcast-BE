package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.RatingResponseDto;
import java.util.UUID;

/** Service for managing episode ratings */
public interface RatingService {

  /**
   * Submit or update a user's rating for an episode. Creates a new rating if none exists, or
   * updates existing rating. User is identified by the requesting username from authentication.
   *
   * @param episodeId The unique identifier of the episode being rated
   * @param rating Rating value (typically 1-5 stars)
   * @param requestingUsername The username of the user making the request
   * @return RatingResponseDto containing the submitted rating and updated episode statistics
   */
  RatingResponseDto rateEpisode(UUID episodeId, int rating, String requestingUsername);
}
