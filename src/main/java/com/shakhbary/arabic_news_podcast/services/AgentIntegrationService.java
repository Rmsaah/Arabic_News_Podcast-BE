package com.shakhbary.arabic_news_podcast.services;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.shakhbary.arabic_news_podcast.models.Article;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.repositories.ArticleRepository;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AgentIntegrationService {

    private final ArticleRepository articleRepository;
    private final EpisodeRepository episodeRepository;
    private final RestTemplate restTemplate;
    
    private static final String AGENT_BASE_URL = "http://localhost:8001/api";

    @Autowired
    public AgentIntegrationService(ArticleRepository articleRepository, 
                                   EpisodeRepository episodeRepository) {
        this.articleRepository = articleRepository;
        this.episodeRepository = episodeRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Step 1: Scrape news from AlRiyadh and save as Articles
     */
    public List<Article> scrapeAndSaveArticles() {
        try {
            log.info("🔍 Starting news scraping from AlRiyadh...");
            String url = AGENT_BASE_URL + "/scrape-news";
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> articles = (List<Map<String, Object>>) body.get("articles");
                
                List<Article> savedArticles = new ArrayList<>();
                
                for (Map<String, Object> articleData : articles) {
                    String title = (String) articleData.get("title");
                    
                    // Skip duplicates
                    if (articleRepository.existsByTitle(title)) {
                        log.info("⏭️  Article already exists: {}", title);
                        continue;
                    }
                    
                    Article article = new Article();
                    article.setTitle(title);
                    article.setContentRaw((String) articleData.get("description_fusha"));
                    article.setContentCleaned((String) articleData.get("description_fusha"));
                    article.setPublisher("AlRiyadh");
                    article.setUrlPath("https://www.alriyadh.com"); // Default
                    article.setStatus("SCRAPED");
                    
                    // Parse publish date
                    String dateStr = (String) articleData.get("date");
                    try {
                        article.setPublishedAt(parsePublishDate(dateStr));
                    } catch (Exception e) {
                        article.setPublishedAt(OffsetDateTime.now());
                    }
                    
                    Article saved = articleRepository.save(article);
                    savedArticles.add(saved);
                    log.info("✅ Saved article: {}", title);
                }
                
                log.info("🎉 Saved {} new articles", savedArticles.size());
                return savedArticles;
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("❌ Error scraping articles: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Step 2: Generate podcast script (convert to Saudi dialect) for an article
     */
    public Episode generatePodcastScript(UUID articleId) {
        try {
            Optional<Article> articleOpt = articleRepository.findById(articleId);
            
            if (articleOpt.isEmpty()) {
                log.error("❌ Article not found: {}", articleId);
                return null;
            }
            
            Article article = articleOpt.get();
            
            // Check if episode already exists
            if (!article.getEpisodes().isEmpty()) {
                log.info("⏭️  Episode already exists for article: {}", article.getTitle());
                return article.getEpisodes().get(0);
            }
            
            log.info("🎙️  Generating podcast script for: {}", article.getTitle());
            
            // Call agent to convert to dialect
            String podcastScript = convertToDialect(article.getContentCleaned());
            
            if (podcastScript == null || podcastScript.equals("FAILED_GENERATION")) {
                article.setStatus("FAILED");
                articleRepository.save(article);
                log.error("❌ Failed to generate script for: {}", article.getTitle());
                return null;
            }
            
            // Create Episode
            Episode episode = new Episode();
            episode.setArticle(article);
            episode.setTitle(article.getTitle());
            episode.setDescription("بودكاست: " + article.getTitle());
            episode.setTranscript(podcastScript); // This is the Saudi dialect script!
            
            Episode savedEpisode = episodeRepository.save(episode);
            
            // Update article status
            article.setStatus("SCRIPT_GENERATED");
            articleRepository.save(article);
            
            log.info("✅ Generated podcast script for: {}", article.getTitle());
            return savedEpisode;
            
        } catch (Exception e) {
            log.error("❌ Error generating script for article {}: {}", articleId, e.getMessage());
            return null;
        }
    }

    /**
     * Step 3: Process all scraped articles (generate scripts for all)
     */
    public List<Episode> processAllScrapedArticles() {
        List<Article> scrapedArticles = articleRepository.findByStatus("SCRAPED");
        List<Episode> generatedEpisodes = new ArrayList<>();
        
        log.info("📋 Found {} articles to process", scrapedArticles.size());
        
        for (Article article : scrapedArticles) {
            Episode episode = generatePodcastScript(article.getId());
            if (episode != null) {
                generatedEpisodes.add(episode);
            }
            
            // Add small delay to avoid rate limiting
            try {
                Thread.sleep(1000); // 1 second delay between articles
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("🎉 Generated {} podcast scripts", generatedEpisodes.size());
        return generatedEpisodes;
    }

    /**
     * Helper: Call agent to convert Fusha to Saudi dialect
     */
    private String convertToDialect(String fushaText) {
        try {
            String url = AGENT_BASE_URL + "/convert-to-dialect";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> requestBody = Map.of("text", fushaText);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("dialect");
            }
            return "CONVERSION_FAILED";
        } catch (Exception e) {
            log.error("❌ Error converting to dialect: {}", e.getMessage());
            return "CONVERSION_FAILED";
        }
    }

    /**
     * Helper: Parse publish date from RSS feed format
     */
    private OffsetDateTime parsePublishDate(String dateStr) {
        try {
            // RSS date format: "Wed, 09 Nov 2024 10:30:00 +0300"
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            return OffsetDateTime.parse(dateStr, formatter);
        } catch (Exception e) {
            log.warn("⚠️  Could not parse date: {}, using current time", dateStr);
            return OffsetDateTime.now();
        }
    }

    /**
     * Get all articles
     */
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    /**
     * Get articles by status
     */
    public List<Article> getArticlesByStatus(String status) {
        return articleRepository.findByStatus(status);
    }
    
    /**
     * Get all episodes with their articles
     */
    public List<Episode> getAllEpisodes() {
        return episodeRepository.findAll();
    }
}