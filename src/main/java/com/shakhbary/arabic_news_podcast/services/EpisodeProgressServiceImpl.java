package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeProgressUpdateDto;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.models.EpisodeProgress;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeProgressRepository;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EpisodeProgressServiceImpl implements EpisodeProgressService {

    private final UserRepository userRepository;
    private final EpisodeRepository episodeRepository;
    private final EpisodeProgressRepository episodeProgressRepository;

    @Override
    @Transactional
    public EpisodeProgressDto updateProgress(UUID userId, EpisodeProgressUpdateDto updateDto, String requestingUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Authorization check: user can only update their own progress
        validateUserAccess(user.getUsername(), requestingUsername, "update your own progress");

        Episode episode = episodeRepository.findById(updateDto.getEpisodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + updateDto.getEpisodeId()));

        // Find existing completion or create new one
        EpisodeProgress completion = episodeProgressRepository
                .findByUserAndEpisode(userId, updateDto.getEpisodeId())
                .orElse(new EpisodeProgress());

        // Set/update the completion data
        completion.setUser(user);
        completion.setEpisode(episode);
        completion.updatePosition(updateDto.getPositionSeconds());

        if (updateDto.isCompleted()) {
            completion.setCompleted(true);
        }

        completion = episodeProgressRepository.save(completion);

        return convertToDto(completion);
    }

    @Override
    @Transactional(readOnly = true)
    public EpisodeProgressDto getProgress(UUID userId, UUID episodeId, String requestingUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Authorization check: user can only view their own progress
        validateUserAccess(user.getUsername(), requestingUsername, "view your own progress");

        EpisodeProgress completion = episodeProgressRepository
                .findByUserAndEpisode(userId, episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("No progress found for this episode"));

        return convertToDto(completion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpisodeProgressDto> getInProgressEpisodes(UUID userId, String requestingUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Authorization check: user can only view their own in-progress episodes
        validateUserAccess(user.getUsername(), requestingUsername, "view your own in-progress episodes");

        List<EpisodeProgress> inProgress = episodeProgressRepository.findInProgressEpisodes(userId);
        return inProgress.stream()
                .filter(ec -> !ec.isCompleted()) // Only truly in-progress episodes
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EpisodeProgressDto> getCompletedEpisodes(UUID userId, Pageable pageable) {
        // This would need a new repository method - for now, throw not implemented
        throw new UnsupportedOperationException("Paginated completed episodes not yet implemented");
    }

    @Override
    @Transactional(readOnly = true)
    public EpisodeAnalyticsDto getEpisodeAnalytics(UUID episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + episodeId));

        Double avgCompletion = episodeProgressRepository.findAverageCompletionForEpisode(episodeId);
        List<EpisodeProgress> dropOffs = episodeProgressRepository.findCommonDropOffPoints(episodeId);

        List<DropOffPoint> dropOffPoints = dropOffs.stream()
                .map(ec -> {
                    int minute = (int) (ec.getLastPositionSeconds() / 60);
                    return new DropOffPoint(
                            minute,
                            1L, // This would need proper grouping in the query
                            String.format("%d:%02d", minute, (int)(ec.getLastPositionSeconds() % 60))
                    );
                })
                .toList();

        return new EpisodeAnalyticsDto(
                episodeId,
                episode.getTitle(),
                avgCompletion != null ? avgCompletion : 0.0,
                0L, // Would need separate query for total plays
                dropOffPoints
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserListeningStatsDto getUserListeningStats(UUID userId, String requestingUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Authorization check: user can only view their own listening stats
        validateUserAccess(user.getUsername(), requestingUsername, "view your own listening stats");

        // Get total listening time from User model (accurate tracking)
        long totalSeconds = user.getSecondsListened();
        long completedCount = episodeProgressRepository.countCompletedEpisodesByUser(userId);
        List<EpisodeProgress> inProgress = episodeProgressRepository.findInProgressEpisodes(userId);
        long inProgressCount = inProgress.stream().filter(ec -> !ec.isCompleted()).count();

        // Calculate average completion rate
        List<EpisodeProgress> allCompletions = episodeProgressRepository.findAllByUserOrdered(userId);
        double avgCompletion = allCompletions.stream()
                .mapToDouble(EpisodeProgress::calculateCompletionPercentage)
                .average()
                .orElse(0.0);

        String formattedTime = formatDuration(totalSeconds);

        return new UserListeningStatsDto(
                userId,
                totalSeconds,
                completedCount,
                inProgressCount,
                avgCompletion,
                formattedTime
        );
    }

    private EpisodeProgressDto convertToDto(EpisodeProgress completion) {
        Episode episode = completion.getEpisode();

        // Calculate completion percentage on-the-fly
        double completionPercentage = completion.calculateCompletionPercentage();

        EpisodeProgressDto dto = new EpisodeProgressDto(
                completion.getId(),
                episode.getId(),
                episode.getTitle(),
                completion.getLastPositionSeconds(),
                completionPercentage,  // Calculated, not stored
                completion.isCompleted(),
                completion.getPlayCount(),
                completion.getLastPlayedDate()
        );

        // Set calculated fields
        dto.setRemainingSeconds(completion.getRemainingSeconds());
        dto.setFormattedPosition(completion.getFormattedPosition());
        dto.setFormattedRemaining(formatDuration(completion.getRemainingSeconds()) + " remaining");

        return dto;
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }

    /**
     * Validates that the requesting user has permission to access the target user's data.
     * Access is granted if:
     * 1. The requesting user is the owner (targetUsername equals requestingUsername), OR
     * 2. The requesting user has ROLE_ADMIN
     *
     * @param targetUsername The username of the user whose data is being accessed
     * @param requestingUsername The username of the user making the request
     * @param action Descriptive action string for error message (e.g., "view your own progress")
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