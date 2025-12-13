package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.services.EpisodeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for homepage and discovery features. Provides endpoints for featured content and
 * recommendations.
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

  private final EpisodeService episodeService;

  /**
   * Get daily featured episodes for the homepage. Returns a curated list of episodes for user
   * discovery.
   *
   * @param limit Number of episodes to return (min 1, max 10), default 5
   * @return List of daily featured episodes
   */
  @GetMapping("/daily")
  public List<EpisodeDto> daily(@RequestParam(defaultValue = "5") Integer limit) {
    log.info("Getting daily featured episodes. limit is: {}", limit);
    if (limit == null) {
      limit = 5;
    }
    int safeLimit = Math.min(Math.max(limit, 1), 10);
    return episodeService.listDailyEpisodes(safeLimit);
  }
}
