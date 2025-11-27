// package com.shakhbary.arabic_news_podcast.services.Imp;

// import java.time.OffsetDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.UUID;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import com.shakhbary.arabic_news_podcast.models.Article;
// import com.shakhbary.arabic_news_podcast.models.Episode;
// import com.shakhbary.arabic_news_podcast.repositories.ArticleRepository;
// import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
// import com.shakhbary.arabic_news_podcast.services.EpisodeAutomationService;

// import lombok.extern.slf4j.Slf4j;

// @Service
// @Slf4j
// public class AgentIntegrationService {

//     private final ArticleRepository articleRepository;
//     private final EpisodeRepository episodeRepository;
//     private final EpisodeAutomationService episodeAutomationService;
//     private final RestTemplate restTemplate;
    
//     private static final String AGENT_BASE_URL = "http://localhost:8001/api";

//     @Autowired
//     public AgentIntegrationService(
//             ArticleRepository articleRepository, 
//             EpisodeRepository episodeRepository,
//             EpisodeAutomationService episodeAutomationService) {
//         this.articleRepository = articleRepository;
//         this.episodeRepository = episodeRepository;
//         this.episodeAutomationService = episodeAutomationService;
//         this.restTemplate = new RestTemplate();
//     }

//     /**
//      * Step 1: Scrape news from AlRiyadh and save as Articles
//      */
//     public List<Article> scrapeAndSaveArticles() {
//         try {
//             log.info("🔍 Starting news scraping from AlRiyadh...");
//             String url = AGENT_BASE_URL + "/scrape-news";
            
//             ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
//             if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                 Map<String, Object> body = response.getBody();
//                 List<Map<String, Object>> articles = (List<Map<String, Object>>) body.get("articles");
                
//                 List<Article> savedArticles = new ArrayList<>();
                
//                 for (Map<String, Object> articleData : articles) {
//                     String title = (String) articleData.get("title");
                    
//                     // Skip duplicates
//                     if (articleRepository.existsByTitle(title)) {
//                         log.info("⏭️  Article already exists: {}", title);
//                         continue;
//                     }
                    
//                     Article article = new Article();
//                     article.setTitle(title);
//                     article.setContentRaw((String) articleData.get("description_fusha"));
//                     article.setContentCleaned((String) articleData.get("description_fusha"));
//                     article.setPublisher("AlRiyadh");
//                     article.setUrlPath("https://www.alriyadh.com");
//                     article.setStatus("SCRAPED");
                    
//                     // Parse publish date
//                     String dateStr = (String) articleData.get("date");
//                     try {
//                         article.setPublishedAt(parsePublishDate(dateStr));
//                     } catch (Exception e) {
//                         article.setPublishedAt(OffsetDateTime.now());
//                     }
                    
//                     Article saved = articleRepository.save(article);
//                     savedArticles.add(saved);
//                     log.info("✅ Saved article: {}", title);
//                 }
                
//                 log.info("🎉 Saved {} new articles", savedArticles.size());
//                 return savedArticles;
//             }
            
//             return Collections.emptyList();
//         } catch (Exception e) {
//             log.error("❌ Error scraping articles: {}", e.getMessage());
//             return Collections.emptyList();
//         }
//     }

//     /**
//      * Step 2: Process article through full pipeline (LLM + TTS + GCS)
//      * Returns Episode created by EpisodeAutomationService
//      */
//     public Episode processArticleFull(UUID articleId) {
//         try {
//             Optional<Article> articleOpt = articleRepository.findById(articleId);
            
//             if (articleOpt.isEmpty()) {
//                 log.error("❌ Article not found: {}", articleId);
//                 return null;
//             }
            
//             Article article = articleOpt.get();
            
//             // Check if episode already exists
//             if (!article.getEpisodes().isEmpty()) {
//                 log.info("⏭️  Episode already exists for article: {}", article.getTitle());
//                 return article.getEpisodes().get(0);
//             }
            
//             log.info("🚀 Processing article through full pipeline: {}", article.getTitle());
//             article.setStatus("PROCESSING");
//             articleRepository.save(article);
            
//             // Call Python agent full pipeline
//             String url = AGENT_BASE_URL + "/process-article-full";
            
//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(MediaType.APPLICATION_JSON);
            
//             Map<String, String> requestBody = Map.of(
//                 "title", article.getTitle(),
//                 "content", article.getContentCleaned(),
//                 "author", article.getAuthor() != null ? article.getAuthor() : "",
//                 "publisher", article.getPublisher(),
//                 "category", article.getCategory() != null ? article.getCategory() : "news",
//                 "date", article.getPublishedAt().toString()
//             );
            
//             HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
//             ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
//             if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                 Map<String, Object> responseBody = response.getBody();
//                 Boolean success = (Boolean) responseBody.get("success");
                
//                 if (Boolean.TRUE.equals(success)) {
//                     Map<String, Object> episodeData = (Map<String, Object>) responseBody.get("data");
                    
//                     // Convert to JSON string for EpisodeAutomationService
//                     String episodeJson = new com.fasterxml.jackson.databind.ObjectMapper()
//                             .writeValueAsString(episodeData);
                    
//                     log.info("📦 Calling EpisodeAutomationService with data...");
                    
//                     // Use your friend's service to save to database
//                     var episodeDto = episodeAutomationService.processEpisodeFromJson(episodeJson);
                    
//                     // Update article status
//                     article.setStatus("COMPLETED");
//                     articleRepository.save(article);
                    
//                     log.info("✅ Episode created successfully: {}", episodeDto.getTitle());
                    
//                     // Return the created episode
//                     return episodeRepository.findById(episodeDto.getId()).orElse(null);
//                 } else {
//                     article.setStatus("FAILED");
//                     articleRepository.save(article);
//                     log.error("❌ Agent returned failure");
//                     return null;
//                 }
//             }
            
//             article.setStatus("FAILED");
//             articleRepository.save(article);
//             return null;
            
//         } catch (Exception e) {
//             log.error("❌ Error processing article {}: {}", articleId, e.getMessage());
//             e.printStackTrace();
//             return null;
//         }
//     }

//     /**
//      * Step 3: Process all scraped articles
//      */
//     public List<Episode> processAllScrapedArticles() {
//         List<Article> scrapedArticles = articleRepository.findByStatus("SCRAPED");
//         List<Episode> generatedEpisodes = new ArrayList<>();
        
//         log.info("📋 Found {} articles to process", scrapedArticles.size());
        
//         for (Article article : scrapedArticles) {
//             try {
//                 Episode episode = processArticleFull(article.getId());
//                 if (episode != null) {
//                     generatedEpisodes.add(episode);
//                 }
                
//                 // Small delay to avoid overwhelming the agent
//                 Thread.sleep(2000);
//             } catch (Exception e) {
//                 log.error("❌ Error processing article {}: {}", article.getId(), e.getMessage());
//             }
//         }
        
//         log.info("🎉 Generated {} episodes", generatedEpisodes.size());
//         return generatedEpisodes;
//     }

//     /**
//      * Helper: Parse publish date from RSS feed format
//      */
//     private OffsetDateTime parsePublishDate(String dateStr) {
//         try {
//             DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
//             return OffsetDateTime.parse(dateStr, formatter);
//         } catch (Exception e) {
//             log.warn("⚠️  Could not parse date: {}, using current time", dateStr);
//             return OffsetDateTime.now();
//         }
//     }

//     /**
//      * Get all articles
//      */
//     public List<Article> getAllArticles() {
//         return articleRepository.findAll();
//     }

//     /**
//      * Get articles by status
//      */
//     public List<Article> getArticlesByStatus(String status) {
//         return articleRepository.findByStatus(status);
//     }
    
//     /**
//      * Get all episodes
//      */
//     public List<Episode> getAllEpisodes() {
//         return episodeRepository.findAll();
//     }
// }