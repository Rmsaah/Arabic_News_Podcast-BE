package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.services.EpisodeProgressService;
import com.shakhbary.arabic_news_podcast.services.EpisodeProgressService.EpisodeAnalyticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin REST controller for episode progress analytics.
 * All endpoints in this controller require ADMIN role (secured by /api/admin/** pattern in SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/progress")
@RequiredArgsConstructor
public class AdminProgressController {

    private final EpisodeProgressService episodeProgressService;

    /**
     * Get analytics for an episode (admin-only endpoint).
     * Returns statistics like total listens, completion rate, average progress, drop-off points.
     *
     * @param episodeId Episode ID
     * @return Episode analytics data
     */
    @GetMapping("/episodes/{episodeId}/analytics")
    public EpisodeAnalyticsDto getEpisodeAnalytics(@PathVariable UUID episodeId) {
        return episodeProgressService.getEpisodeAnalytics(episodeId);
    }
}
