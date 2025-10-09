package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.RatingRequestDto;
import com.shakhbary.arabic_news_podcast.dtos.RatingResponseDto;
import com.shakhbary.arabic_news_podcast.services.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing episode ratings.
 * Allows users to rate podcast episodes on a 1-5 scale.
 */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * Submit a rating for an episode.
     * Users can rate episodes from 1 (lowest) to 5 (highest).
     * If a user has already rated an episode, the rating will be updated.
     *
     * @param request Rating request containing user ID, episode ID, and rating value (1-5)
     * @return Rating response with confirmation
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RatingResponseDto rateEpisode(@RequestBody @Valid RatingRequestDto request) {
        return ratingService.rateEpisode(
                request.userId(),
                request.episodeId(),
                request.rating()
        );
    }
}
