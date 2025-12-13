package com.shakhbary.arabic_news_podcast.validator;

import com.shakhbary.arabic_news_podcast.dtos.RatingRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;


@Component
public class RatingValidator {

    public void validateRatingRequest(RatingRequestDto ratingRequestDto) {
        if (ObjectUtils.isEmpty(ratingRequestDto)) {
            throw new IllegalArgumentException("Rating request cannot be null");
        }

        if (ObjectUtils.isEmpty(ratingRequestDto.episodeId())) {
            throw new IllegalArgumentException("Episode id cannot be null");
        }

        if (ObjectUtils.isEmpty(ratingRequestDto.rating())) {
            throw new IllegalArgumentException("Rating cannot be null");
        }
        validateRating(ratingRequestDto.rating());
    }

    public void validateRating(int ratingValue) {
        if (ratingValue < 1 || ratingValue > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
