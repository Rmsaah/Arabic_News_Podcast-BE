package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.RatingResponseDto;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.models.Rating;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.repositories.RatingRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final EpisodeRepository episodeRepository;

    @Override
    @Transactional
    public RatingResponseDto rateEpisode(UUID userId, UUID episodeId, int ratingValue) {
        if (ratingValue < 1 || ratingValue > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + episodeId));

        Rating rating = ratingRepository.findByUserAndEpisode(userId, episodeId);
        boolean isUpdate = rating != null;

        if (rating == null) {
            rating = new Rating();
            rating.setUser(user);
            rating.setEpisode(episode);
        }
        rating.setRating(ratingValue);
        rating.setRatedAt(OffsetDateTime.now());

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
