package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeCreateRequestDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.services.EpisodeService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing podcast episodes.
 * Provides endpoints for listing, searching, retrieving, and creating episodes.
 */
@RestController
@RequestMapping("/api/episodes")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;

    /**
     * Get a paginated list of all episodes.
     *
     * @param page Page number (0-based), default 0
     * @param size Page size (max 100), default 20
     * @return Paginated list of episodes
     */
    @GetMapping
    public Page<EpisodeDto> listEpisodes(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return episodeService.listEpisodes(pageable);
    }

    /**
     * Get a specific episode by ID.
     *
     * @param id Episode ID
     * @return Episode details
     */
    @GetMapping("/{id}")
    public EpisodeDto getEpisode(@PathVariable("id") UUID id) {
        return episodeService.getEpisode(id);
    }

    /**
     * Create a new episode.
     *
     * @param request Episode creation request with all required fields
     * @return Created episode with generated ID
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EpisodeDto createEpisode(@RequestBody @jakarta.validation.Valid EpisodeCreateRequestDto request) {
        return episodeService.createEpisode(request);
    }

    /**
     * Search for episodes by title and/or category.
     *
     * @param title Optional title search term (partial match)
     * @param category Optional category filter (exact match)
     * @param page Page number (0-based), default 0
     * @param size Page size (max 100), default 20
     * @return Paginated search results
     */
    @GetMapping("/search")
    public Page<EpisodeDto> search(@RequestParam(required = false) String title,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return episodeService.searchEpisodes(title, category, pageable);
    }
}