package com.shakhbary.arabic_news_podcast.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSampleDto {
  @JsonProperty("episode")
  EpisodeDto episodeDto;

  @JsonProperty("article")
  ArticleDto articleDto;

  @JsonProperty("audio")
  AudioDto audioDto;
}
