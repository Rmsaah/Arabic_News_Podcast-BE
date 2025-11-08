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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public RatingResponseDto rateEpisode(UUID userId, UUID episodeId, int ratingValue, String requestingUsername) {
        if (ratingValue < 1 || ratingValue > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Authorization check: user can only rate on their own behalf
        validateUserAccess(user.getUsername(), requestingUsername, "rate episodes on your own behalf");

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

    /**
     * Validates that the requesting user has permission to access the target user's data.
     * Access is granted if:
     * 1. The requesting user is the owner (targetUsername equals requestingUsername), OR
     * 2. The requesting user has ROLE_ADMIN
     *
     * @param targetUsername The username of the user whose data is being accessed
     * @param requestingUsername The username of the user making the request
     * @param action Descriptive action string for error message (e.g., "rate episodes on your own behalf")
     * @throws ResponseStatusException with HTTP 403 if access is denied
     */
    private void validateUserAccess(String targetUsername, String requestingUsername, String action) {
        // Owner can always access their own data
        if (targetUsername.equals(requestingUsername)) {
            return;
        }

        // Check if requesting user has admin role
        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only " + action
            );
        }
    }
}
