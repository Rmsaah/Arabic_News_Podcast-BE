package com.shakhbary.arabic_news_podcast.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shakhbary.arabic_news_podcast.models.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    
    // Find articles by status
    List<Article> findByStatus(String status);
    
    // Check if article with same title exists (avoid duplicates)
    boolean existsByTitle(String title);
    
    // Find article by title
    Optional<Article> findByTitle(String title);
    
    // Find by publisher
    List<Article> findByPublisher(String publisher);
}