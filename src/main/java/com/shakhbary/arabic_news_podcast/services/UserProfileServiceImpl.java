package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeHistoryDto;
import com.shakhbary.arabic_news_podcast.dtos.UserProfileDto;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.models.EpisodeProgress;
import com.shakhbary.arabic_news_podcast.models.Rating;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeProgressRepository;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.repositories.RatingRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final EpisodeProgressRepository episodeProgressRepository;
    private final EpisodeRepository episodeRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(UUID userId, String requestingUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<Rating> ratings = ratingRepository.findAllByUserOrdered(userId);

        // Get total listening time directly from user model
        long totalSeconds = user.getSecondsListened();

        // Count actually completed episodes (regardless of rating)
        long completedEpisodes = episodeProgressRepository.countCompletedEpisodesByUser(userId);

        // Create recent ratings list (limit to last 10)
        List<UserProfileDto.UserRatingDto> recentRatings = ratings.stream()
                .limit(10)
                .map(r -> new UserProfileDto.UserRatingDto(
                        r.getEpisode().getId(),
                        r.getEpisode().getTitle(),
                        r.getRating(),
                        r.getRatingDate()
                )).toList();

        // Build episode history: ALL episodes with progress (completed or in-progress)
        List<EpisodeProgress> progressRecords = episodeProgressRepository.findAllByUserOrdered(userId);

        // Create a map of episodeId -> rating for quick lookup
        Map<UUID, Rating> episodeRatings = ratings.stream()
                .collect(Collectors.toMap(
                        r -> r.getEpisode().getId(),
                        r -> r,
                        (existing, replacement) -> existing // keep first if duplicate
                ));

        // Build history list from all progress records (not just completed)
        List<EpisodeHistoryDto> episodeHistory = progressRecords.stream()
                .limit(20) // Show last 20 episodes with any progress
                .map(progress -> {
                    Episode episode = progress.getEpisode();
                    Rating rating = episodeRatings.get(episode.getId());

                    // Determine rating status string
                    String ratingStatus;
                    Integer ratingValue = null;
                    OffsetDateTime ratingDate = null;

                    if (rating != null) {
                        ratingValue = rating.getRating();
                        ratingDate = rating.getRatingDate();
                        ratingStatus = ratingValue == 1 ? "1 star" : ratingValue + " stars";
                    } else {
                        ratingStatus = "Not Rated";
                    }

                    return new EpisodeHistoryDto(
                            episode.getId(),
                            episode.getTitle(),
                            episode.getImageUrl(),
                            progress.getLastPositionSeconds(),
                            progress.calculateCompletionPercentage(),
                            progress.isCompleted(),
                            progress.getPlayCount(),
                            progress.getLastPlayedDate(),
                            ratingStatus,
                            ratingValue,
                            ratingDate
                    );
                }).toList();

        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreationDate(),
                (int) completedEpisodes,
                totalSeconds,
                recentRatings,
                episodeHistory
        );
    }

    @Override
    @Transactional
    public void trackListeningTime(long secondsListened, String requestingUsername) {
        User user = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requestingUsername));

        user.addListeningTime(secondsListened);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateEpisodeProgress(UUID episodeId, long positionSeconds, String requestingUsername) {
        User user = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requestingUsername));

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + episodeId));

        // Find existing completion or create new one
        EpisodeProgress completion = episodeProgressRepository
                .findByUserAndEpisode(user.getId(), episodeId)
                .orElse(new EpisodeProgress());

        // Set/update the completion data
        completion.setUser(user);
        completion.setEpisode(episode);
        completion.updatePosition(positionSeconds); // This will calculate percentage and handle completion logic

        episodeProgressRepository.save(completion);
    }

    @Override
    @Transactional
    public void markEpisodeCompleted(UUID episodeId, long positionSeconds, String requestingUsername) {
        User user = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requestingUsername));

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + episodeId));

        // Find existing completion or create new one
        EpisodeProgress completion = episodeProgressRepository
                .findByUserAndEpisode(user.getId(), episodeId)
                .orElse(new EpisodeProgress());

        completion.setUser(user);
        completion.setEpisode(episode);
        completion.updatePosition(positionSeconds);
        completion.setCompleted(true); // Explicitly mark as completed

        episodeProgressRepository.save(completion);
    }
}
