package com.shakhbary.arabic_news_podcast.repositories;

import com.shakhbary.arabic_news_podcast.models.EpisodeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EpisodeProgressRepository extends JpaRepository<EpisodeProgress, UUID> {

    @Query("SELECT COUNT(ep) FROM EpisodeProgress ep WHERE ep.user.id = :userId AND ep.isCompleted = true")
    long countCompletedEpisodesByUser(@Param("userId") UUID userId);

    @Query("SELECT ep FROM EpisodeProgress ep WHERE ep.user.id = :userId ORDER BY ep.lastPlayedDate DESC")
    List<EpisodeProgress> findAllByUserOrdered(@Param("userId") UUID userId);

    @Query("SELECT ep FROM EpisodeProgress ep WHERE ep.user.id = :userId AND ep.episode.id = :episodeId")
    Optional<EpisodeProgress> findByUserAndEpisode(@Param("userId") UUID userId, @Param("episodeId") UUID episodeId);

    boolean existsByUserIdAndEpisodeId(UUID userId, UUID episodeId);

    // New queries for position-based tracking
    @Query("SELECT ep FROM EpisodeProgress ep WHERE ep.user.id = :userId AND ep.lastPositionSeconds > 0 AND ep.isCompleted = false ORDER BY ep.lastPlayedDate DESC")
    List<EpisodeProgress> findInProgressEpisodes(@Param("userId") UUID userId);

    // Calculate average completion percentage: (lastPositionSeconds / episode.audio.duration)
    @Query("SELECT AVG(CAST(ep.lastPositionSeconds AS double) / ep.episode.audio.duration) FROM EpisodeProgress ep WHERE ep.episode.id = :episodeId AND ep.episode.audio.duration > 0")
    Double findAverageCompletionForEpisode(@Param("episodeId") UUID episodeId);

    @Query("SELECT ep FROM EpisodeProgress ep WHERE ep.episode.id = :episodeId AND ep.isCompleted = false GROUP BY FLOOR(ep.lastPositionSeconds / 60) ORDER BY COUNT(*) DESC")
    List<EpisodeProgress> findCommonDropOffPoints(@Param("episodeId") UUID episodeId);
}