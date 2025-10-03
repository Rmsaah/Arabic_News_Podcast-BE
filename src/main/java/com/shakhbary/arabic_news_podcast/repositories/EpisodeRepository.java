package com.shakhbary.arabic_news_podcast.repositories;

import com.shakhbary.arabic_news_podcast.models.Episode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode,UUID> {
    /*
    * description: Finds episodes created after a certain date, sorted by newest first (descending), with pagination
    * after: used for the 'CreatedAtAfter' filter
    * pageable: the input object that dictates which page number and how many items per page we want (e.g., page 2, 10 items)
     */
    Page<Episode> findByCreatedAtAfterOrderByCreatedAtDesc(OffsetDateTime after, Pageable pageable);

    /*
    * description: Advanced search for episodes by title and/or category with pagination.
     */
    @Query("SELECT e FROM Episode e LEFT JOIN e.article a WHERE (:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%',:title,'%'))) AND (:category IS NULL OR (a IS NOT NULL AND LOWER(a.category)=LOWER(:category)))")
    Page<Episode> search(@Param("title") String title,
                         @Param("category") String category,
                         Pageable pageable);
}
