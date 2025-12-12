package com.shakhbary.arabic_news_podcast.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSampleDto {
	EpisodeDto episodeDto;
	ArticleDto articleDto;
	AudioDto audioDto;
}
