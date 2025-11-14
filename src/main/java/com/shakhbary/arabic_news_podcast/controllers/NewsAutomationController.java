package com.shakhbary.arabic_news_podcast.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shakhbary.arabic_news_podcast.models.Article;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.services.AgentIntegrationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/news-automation")
@CrossOrigin(origins = "*")
@Slf4j
public class NewsAutomationController {

    @Autowired
    private AgentIntegrationService agentService;

    /**
     * Step 1: Scrape news from AlRiyadh and save as articles
     */
    @PostMapping("/scrape")
    public ResponseEntity<?> scrapeNews() {
        log.info("API: Scraping news...");
        List<Article> articles = agentService.scrapeAndSaveArticles();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Scraped and saved articles",
            "count", articles.size(),
            "articles", articles
        ));
    }

    /**
     * Step 2: Generate podcast script for a specific article
     */
    @PostMapping("/generate-script/{articleId}")
    public ResponseEntity<?> generateScript(@PathVariable UUID articleId) {
        log.info("API: Generating script for article: {}", articleId);
        Episode episode = agentService.generatePodcastScript(articleId);
        
        if (episode != null) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Podcast script generated",
                "episode", episode
            ));
        }
        
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Failed to generate script"
        ));
    }

    /**
     * Step 3: Process all scraped articles (generate scripts for all)
     */
    @PostMapping("/process-all")
    public ResponseEntity<?> processAllArticles() {
        log.info("API: Processing all scraped articles...");
        List<Episode> episodes = agentService.processAllScrapedArticles();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Processed all articles",
            "count", episodes.size(),
            "episodes", episodes
        ));
    }

    /**
     * Get all articles
     */
    @GetMapping("/articles")
    public ResponseEntity<?> getAllArticles() {
        List<Article> articles = agentService.getAllArticles();
        return ResponseEntity.ok(articles);
    }

    /**
     * Get articles by status
     */
    @GetMapping("/articles/status/{status}")
    public ResponseEntity<?> getArticlesByStatus(@PathVariable String status) {
        List<Article> articles = agentService.getArticlesByStatus(status);
        return ResponseEntity.ok(articles);
    }

    /**
     * Get all episodes
     */
    @GetMapping("/episodes")
    public ResponseEntity<?> getAllEpisodes() {
        List<Episode> episodes = agentService.getAllEpisodes();
        return ResponseEntity.ok(episodes);
    }
}