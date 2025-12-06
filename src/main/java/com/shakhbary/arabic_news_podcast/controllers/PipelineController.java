package com.shakhbary.arabic_news_podcast.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.services.Impl.EpisodeAutomationServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/podcast")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PipelineController {

    private final EpisodeAutomationServiceImpl episodeAutomationService;

    /**
     * Run the complete automated pipeline
     */
    @PostMapping("/run-daily-pipeline")
    public ResponseEntity<?> runDailyPipeline() {
        log.info("API: Running daily podcast pipeline...");
        
        try {
            List<EpisodeDto> episodes = episodeAutomationService.automatedDailyPipeline();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pipeline completed successfully",
                "count", episodes.size(),
                "episodes", episodes
            ));
        } catch (Exception e) {
            log.error("Pipeline failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}