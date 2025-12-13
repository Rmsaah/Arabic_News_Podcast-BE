package com.shakhbary.arabic_news_podcast.services.Impl;

import com.shakhbary.arabic_news_podcast.dtos.RatingResponseDto;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.mappers.RatingMapper;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.models.Rating;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.repositories.RatingRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;
import com.shakhbary.arabic_news_podcast.services.RatingService;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

  private final RatingRepository ratingRepository;
  private final UserRepository userRepository;
  private final EpisodeRepository episodeRepository;
  private final RatingMapper ratingMapper;

  @Override
  @Transactional
  public RatingResponseDto rateEpisode(UUID episodeId, int ratingValue, String requestingUsername) {

    User user = findByUsername(requestingUsername);
    Episode episode = findByEpisodeId(episodeId);

    Rating rating = findOrCreateRating(user, episode);
    boolean isUpdate = rating.getId() != null;

    rating.setRating(ratingValue);
    rating.setRatingDate(OffsetDateTime.now());
    rating = ratingRepository.save(rating);

    RatingResponseDto responseDto = ratingMapper.ratingToRatingResponseDto(rating);
    responseDto.setMessage(
        isUpdate ? "Rating updated successfully" : "Rating created successfully");

    return responseDto;
  }

  private User findByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
  }

  private Episode findByEpisodeId(UUID episodeId) {
    return episodeRepository
        .findById(episodeId)
        .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + episodeId));
  }

  private Rating findOrCreateRating(User user, Episode episode) {
    Rating rating = ratingRepository.findByUserAndEpisode(user.getId(), episode.getId());

    if (rating == null) {
      rating = new Rating();
      rating.setUser(user);
      rating.setEpisode(episode);
    }

    return rating;
  }
}
