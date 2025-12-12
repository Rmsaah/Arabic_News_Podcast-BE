package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.CreateSampleDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.services.EpisodeAutomationService;
import com.shakhbary.arabic_news_podcast.validator.EpisodeAutomationValidator;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for automating podcast episode creation. (Creates all 3 objects (Article + Audio
 * + Episode) at once) Handles bulk episode creation from JSON files and single episode processing.
 *
 * <p>Security: Admin-only endpoints under /api/admin/* should be protected by authentication.
 */
@RestController
@RequestMapping("/api/admin/automation")
@RequiredArgsConstructor
@Slf4j
public class EpisodeAutomationController {

  private final EpisodeAutomationService episodeAutomationService;
  private final EpisodeAutomationValidator episodeAutomationValidator;

  /**
   * Post and process a POJO object containing one or more episodes to be created.
   *
   * @param createSampleDtoList POJO list that holds the episode data
   * @return Response with a list of created episodes
   * @throws IOException if file processing fails
   */
  @PostMapping("/bulk-create-episodes")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<AutomationResponse> bulkCreateEpisodes(
      @RequestBody List<CreateSampleDto> createSampleDtoList) throws IOException {

    // Validation
    episodeAutomationValidator.validateBulkCreation(createSampleDtoList);

    // Process episode creation
    List<EpisodeDto> createdEpisodes =
        episodeAutomationService.createBulkEpisodes(createSampleDtoList);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new AutomationResponse(
                "Successfully processed " + createdEpisodes.size() + " episodes", createdEpisodes));
  }

  /**
   * Process a single episode from JSON payload.
   *
   * @param createSampleDto POJO that holds the episode data
   * @return Response with created episode
   */
  @PostMapping("/create-episode")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<AutomationResponse> createEpisode(
      @RequestBody CreateSampleDto createSampleDto) {

    episodeAutomationValidator.validateSingleEpisodeCreation(createSampleDto);

    EpisodeDto createdEpisode = episodeAutomationService.createEpisode(createSampleDto);

    log.info("Successfully processed single episode: {}", createdEpisode.getTitle());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new AutomationResponse(
                "Successfully processed episode: " + createdEpisode.getTitle(),
                List.of(createdEpisode)));
  }

  /**
   * Response DTO for automation endpoints. Simplified to remove redundant fields (success flag,
   * processedCount).
   *
   * @param message Human-readable success message
   * @param episodes List of created/processed episodes
   */
  public record AutomationResponse(String message, List<EpisodeDto> episodes) {}
}
