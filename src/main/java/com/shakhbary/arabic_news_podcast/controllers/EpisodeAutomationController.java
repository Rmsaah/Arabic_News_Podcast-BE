package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.exceptions.BadRequestException;
import com.shakhbary.arabic_news_podcast.services.EpisodeAutomationService;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * REST controller for automating podcast episode creation. (Creates all 3 objects (Article + Audio + Episode) at once)
 * Handles bulk episode creation from JSON files and single episode processing.
 * <p>
 * Security: Admin-only endpoints under /api/admin/* should be protected by authentication.
 */
@RestController
@RequestMapping("/api/admin/automation")
@Slf4j
public class EpisodeAutomationController {

    private final EpisodeAutomationService episodeAutomationService;

    @Value("${app.automation.allowed-directory:/tmp/episodes}")
    private String allowedDirectory;

    @Value("${app.automation.max-file-size-mb:10}")
    private long maxFileSizeMb;

    public EpisodeAutomationController(EpisodeAutomationService episodeAutomationService) {
        this.episodeAutomationService = episodeAutomationService;
    }

    /**
     * Upload and process a JSON file containing multiple episodes.
     * <p>
     * File requirements:
     * <ul>
     *   <li>Must be a .json file</li>
     *   <li>Maximum size: 10MB (configurable)</li>
     *   <li>Must contain valid episode data array</li>
     * </ul>
     *
     * @param file Multipart JSON file containing episode data
     * @return Response with list of created episodes
     * @throws IOException if file processing fails
     */
    @PostMapping("/upload-json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AutomationResponse> uploadJsonFile(@RequestParam("file") MultipartFile file)
            throws IOException {

        // Validate file
        validateJsonFile(file);

        // Process file with proper resource management
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("episodes", ".json");

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Process the JSON file
            List<EpisodeDto> createdEpisodes = episodeAutomationService
                    .processEpisodesFromJson(tempFile.toString());

            log.info("Successfully processed {} episodes from uploaded JSON file", createdEpisodes.size());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AutomationResponse(
                            "Successfully processed " + createdEpisodes.size() + " episodes",
                            createdEpisodes
                    ));

        } finally {
            // Always cleanup temp file, even if exception occurs
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                    log.debug("Cleaned up temp file: {}", tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temp file: {}", tempFile, e);
                }
            }
        }
    }

    /**
     * Process a single episode from JSON payload.
     *
     * @param episodeJson JSON string containing episode data
     * @return Response with created episode
     */
    @PostMapping("/process-episode")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AutomationResponse> processEpisode(@RequestBody String episodeJson) {

        if (episodeJson == null || episodeJson.isBlank()) {
            throw new BadRequestException("Episode JSON cannot be empty");
        }

        EpisodeDto createdEpisode = episodeAutomationService.processEpisodeFromJson(episodeJson);

        log.info("Successfully processed single episode: {}", createdEpisode.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AutomationResponse(
                        "Successfully processed episode: " + createdEpisode.getTitle(),
                        List.of(createdEpisode)
                ));
    }

    /**
     * Validates uploaded JSON file.
     *
     * @param file The multipart file to validate
     * @throws BadRequestException if validation fails
     */
    private void validateJsonFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".json")) {
            throw new BadRequestException("File must be a JSON file (.json extension required)");
        }

        // Check file size (convert MB to bytes)
        long maxSizeBytes = maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new BadRequestException(
                    String.format("File size (%d bytes) exceeds maximum limit of %d MB",
                            file.getSize(), maxFileSizeMb)
            );
        }

        log.debug("File validation passed: {} ({} bytes)", originalFilename, file.getSize());
    }

    /**
     * Response DTO for automation endpoints.
     * Simplified to remove redundant fields (success flag, processedCount).
     *
     * @param message Human-readable success message
     * @param episodes List of created/processed episodes
     */
    public record AutomationResponse(
            String message,
            List<EpisodeDto> episodes
    ) {}

}