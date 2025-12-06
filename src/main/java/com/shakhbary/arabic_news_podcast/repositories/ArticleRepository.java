package com.shakhbary.arabic_news_podcast.repositories;

import com.shakhbary.arabic_news_podcast.models.Article;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {}
