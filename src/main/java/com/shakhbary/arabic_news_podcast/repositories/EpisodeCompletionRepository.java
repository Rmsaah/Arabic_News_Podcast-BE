package com.shakhbary.arabic_news_podcast.repositories;

import com.shakhbary.arabic_news_podcast.models.EpisodeCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EpisodeCompletionRepository extends JpaRepository<EpisodeCompletion, UUID> {

    @Query("SELECT COUNT(ec) FROM EpisodeCompletion ec WHERE ec.user.id = :userId")
    long countCompletedEpisodesByUser(@Param("userId") UUID userId);

    @Query("SELECT ec FROM EpisodeCompletion ec WHERE ec.user.id = :userId ORDER BY ec.completedAt DESC")
    List<EpisodeCompletion> findAllByUserOrdered(@Param("userId") UUID userId);

    @Query("SELECT ec FROM EpisodeCompletion ec WHERE ec.user.id = :userId AND ec.episode.id = :episodeId")
    Optional<EpisodeCompletion> findByUserAndEpisode(@Param("userId") UUID userId, @Param("episodeId") UUID episodeId);

    boolean existsByUserIdAndEpisodeId(UUID userId, UUID episodeId);
}
