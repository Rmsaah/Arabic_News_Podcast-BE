package com.shakhbary.arabic_news_podcast.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
/*
EpisodeCompletion, is designed to track a user's progress through an episode, which is a key piece of logic for achieving that Plex/Spotify experience
The constraints ensure that no single user can have more than one EpisodeCompletion record for the same episode.
The combination of user_id and episode_id must be unique.
 */
@Table(name = "episodes_completions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "episode_id"}),
        indexes = {
                @Index(name = "idx_completions_user_id", columnList = "user_id"),
                @Index(name = "idx_completions_episode_id", columnList = "episode_id")
        })
public class EpisodeCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @CreatedDate
    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;

    @Column(name = "completion_percentage", nullable = false)
    private double completionPercentage; // e.g., 0.95 for 95% completion
}
