package com.shakhbary.arabic_news_podcast.services.Impl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shakhbary.arabic_news_podcast.dtos.RatingResponseDto;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.models.Rating;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.repositories.RatingRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;
import com.shakhbary.arabic_news_podcast.services.RatingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final EpisodeRepository episodeRepository;

    @Override
    @Transactional
    public RatingResponseDto rateEpisode(UUID episodeId, int ratingValue, String requestingUsername) {
        if (ratingValue < 1 || ratingValue > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        User user = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requestingUsername));

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + episodeId));

        Rating rating = ratingRepository.findByUserAndEpisode(user.getId(), episodeId);
        boolean isUpdate = rating != null;

        if (rating == null) {
            rating = new Rating();
            rating.setUser(user);
            rating.setEpisode(episode);
        }
        rating.setRating(ratingValue);
        rating.setRatingDate(OffsetDateTime.now());

        rating = ratingRepository.save(rating);

        String message = isUpdate ? "Rating updated successfully" : "Rating created successfully";
        return new RatingResponseDto(
                rating.getId(),
                rating.getUser().getId(),
                rating.getEpisode().getId(),
                rating.getRating(),
                message
        );
    }
}
