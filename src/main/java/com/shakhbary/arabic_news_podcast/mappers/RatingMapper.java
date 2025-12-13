package com.shakhbary.arabic_news_podcast.mappers;

import com.shakhbary.arabic_news_podcast.dtos.RatingResponseDto;
import com.shakhbary.arabic_news_podcast.models.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {
  @Mapping(source = "id", target = "ratingId")
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "episode.id", target = "episodeId")
  RatingResponseDto ratingToRatingResponseDto(Rating rating);
}
