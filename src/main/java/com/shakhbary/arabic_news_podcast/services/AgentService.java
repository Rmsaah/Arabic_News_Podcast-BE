package com.shakhbary.arabic_news_podcast.services;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AgentService {
    
    private final RestTemplate restTemplate;
    private static final String AGENT_BASE_URL = "http://localhost:8001/api";
    
    public AgentService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Scrape latest news from AlRiyadh
     */
    public List<Map<String, Object>> scrapeNews() {
        try {
            String url = AGENT_BASE_URL + "/scrape-news";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return (List<Map<String, Object>>) body.get("articles");
            }
            return List.of();
        } catch (Exception e) {
            log.error("Error scraping news: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Convert Fusha text to Saudi dialect
     */
    public String convertToDialect(String fushaText) {
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
            log.error("Error converting to dialect: {}", e.getMessage());
            return "CONVERSION_FAILED";
        }
    }
    
    /**
     * Process complete article (scrape + convert)
     */
    public Map<String, Object> processArticle(String title, String description) {
        try {
            String url = AGENT_BASE_URL + "/process-article";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> requestBody = Map.of(
                "title", title,
                "description", description
            );
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return Map.of("success", false);
        } catch (Exception e) {
            log.error("Error processing article: {}", e.getMessage());
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}