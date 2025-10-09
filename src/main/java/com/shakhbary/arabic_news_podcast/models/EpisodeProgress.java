package com.shakhbary.arabic_news_podcast.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
/*
EpisodeProgress, is designed to track a user's progress through an episode, which is a key piece of logic for achieving that Plex/Spotify experience
The constraints ensure that no single user can have more than one EpisodeProgress record for the same episode.
The combination of user_id and episode_id must be unique.
 */
@Table(name = "episode_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "episode_id"}),
        indexes = {
                @Index(name = "idx_progress_user_id", columnList = "user_id"),
                @Index(name = "idx_progress_episode_id", columnList = "episode_id")
        })
public class EpisodeProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /* FOREIGN KEYS */

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    /* DATA */

    @CreatedDate
    @Column(name = "last_played_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastPlayedAt = OffsetDateTime.now();

    // Store exact second where user stopped (primary data)
    @Column(name = "last_position_seconds", nullable = false)
    private long lastPositionSeconds; // e.g., 1245 (20 minutes 45 seconds)

    // Track if user actually finished the episode (useful for completion analytics)
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    // Track how many times user has listened to this episode
    @Column(name = "play_count", nullable = false)
    private int playCount = 1;

    // Utility methods for dynamic calculations

    /**
     * Calculate completion percentage based on current position and episode duration
     */
    public double calculateCompletionPercentage() {
        if (episode == null || episode.getAudio() == null) {
            return 0.0;
        }
        long totalDuration = episode.getAudio().getDuration();
        if (totalDuration <= 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) lastPositionSeconds / totalDuration);
    }

    /**
     * Update position and auto-mark as completed if threshold reached
     */
    public void updatePosition(long positionSeconds) {
        this.lastPositionSeconds = positionSeconds;
        this.lastPlayedAt = OffsetDateTime.now();

        // Auto-mark as completed if user reached 95% or more
        double completionPercentage = calculateCompletionPercentage();
        if (completionPercentage >= 0.95) {
            this.isCompleted = true;
        }
    }

    /**
     * Get remaining time in seconds
     */
    public long getRemainingSeconds() {
        if (episode == null || episode.getAudio() == null) {
            return 0;
        }
        return Math.max(0, episode.getAudio().getDuration() - lastPositionSeconds);
    }

    /**
     * Format position as MM:SS or HH:MM:SS
     */
    public String getFormattedPosition() {
        long hours = lastPositionSeconds / 3600;
        long minutes = (lastPositionSeconds % 3600) / 60;
        long seconds = lastPositionSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
