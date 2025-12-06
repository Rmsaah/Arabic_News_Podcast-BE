package com.shakhbary.arabic_news_podcast.repositories;

import com.shakhbary.arabic_news_podcast.models.Rating;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

  @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.episode.id = :episodeId")
  Double findAverageRatingForEpisode(@Param("episodeId") UUID episodeId);

  @Query("SELECT COUNT(r) FROM Rating r WHERE r.episode.id = :episodeId")
  long countRatingsForEpisode(@Param("episodeId") UUID episodeId);

  @Query("SELECT r FROM Rating r WHERE r.user.id = :userId ORDER BY r.ratingDate DESC")
  List<Rating> findAllByUserOrdered(@Param("userId") UUID userId);

  @Query("SELECT r FROM Rating r WHERE r.user.id = :userId AND r.episode.id = :episodeId")
  Rating findByUserAndEpisode(@Param("userId") UUID userId, @Param("episodeId") UUID episodeId);
}
