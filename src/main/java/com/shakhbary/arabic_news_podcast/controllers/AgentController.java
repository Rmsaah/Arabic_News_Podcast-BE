package com.shakhbary.arabic_news_podcast.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shakhbary.arabic_news_podcast.services.AgentService;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
public class AgentController {
    
    @Autowired
    private AgentService agentService;
    
    @GetMapping("/scrape")
    public ResponseEntity<?> scrapeNews() {
        List<Map<String, Object>> articles = agentService.scrapeNews();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", articles.size(),
            "articles", articles
        ));
    }
    
    @PostMapping("/convert")
    public ResponseEntity<?> convertToDialect(@RequestBody Map<String, String> request) {
        String fushaText = request.get("text");
        String dialectText = agentService.convertToDialect(fushaText);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "dialect", dialectText
        ));
    }
    
    @PostMapping("/process")
    public ResponseEntity<?> processArticle(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String description = request.get("description");
        
        Map<String, Object> result = agentService.processArticle(title, description);
        return ResponseEntity.ok(result);
    }
}