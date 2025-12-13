package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.services.EpisodeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing podcast episodes.
 *
 * <p>Public endpoints: Listing and retrieving episodes (GET operations) Admin endpoints: Creating
 * episodes (POST operations) - should be under /api/admin/episodes
 *
 * <p>Note: Episode creation via POST /api/admin/episodes should be protected by authentication. For
 * bulk episode creation with automatic Article/Audio creation, use EpisodeAutomationController.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class EpisodeController {

  private final EpisodeService episodeService;

  /**
   * Get a paginated list of all episodes. Public endpoint - no authentication required.
   *
   * @param page Page number (0-based), default 0
   * @param size Page size (max 100), default 20
   * @return Paginated list of episodes
   */
  @GetMapping("/api/episodes")
  public Page<EpisodeDto> listEpisodes(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {
    log.info("Listing episodes. Received parameters - page: {}, size: {}", page, size);
    Pageable pageable = PageRequest.of(page, Math.min(size, 100));
    return episodeService.listEpisodes(pageable);
  }

  /**
   * Get a specific episode by ID. Public endpoint - no authentication required.
   *
   * @param id Episode ID
   * @return Episode details
   */
  @GetMapping("/api/episodes/{id}")
  public EpisodeDto getEpisode(@PathVariable("id") UUID id) {
    return episodeService.getEpisode(id);
  }

  /**
   * Search for episodes by title and/or category. Public endpoint - no authentication required.
   *
   * @param title Optional title search term (partial match)
   * @param category Optional category filter (exact match)
   * @param page Page number (0-based), default 0
   * @param size Page size (max 100), default 20
   * @return Paginated search results
   */
  @GetMapping("/api/episodes/search")
  public Page<EpisodeDto> search(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String category,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {
    log.info(
        "Searching episodes. Parameters - title: '{}', category: '{}', page: {}, size: {}",
        title,
        category,
        page,
        size);
    Pageable pageable = PageRequest.of(page, Math.min(size, 100));
    return episodeService.searchEpisodes(title, category, pageable);
  }
}
