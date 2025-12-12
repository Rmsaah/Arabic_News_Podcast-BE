package com.shakhbary.arabic_news_podcast.mappers;

import com.shakhbary.arabic_news_podcast.dtos.CreateSampleDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.models.Episode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EpisodeMapper {
  @Mapping(source = "audio.urlPath", target = "audioUrlPath")
  @Mapping(source = "audio.duration", target = "durationSeconds")
  @Mapping(source = "article.id", target = "articleId")
  @Mapping(source = "article.title", target = "articleTitle")
  @Mapping(source = "article.author", target = "articleAuthor")
  @Mapping(source = "article.publisher", target = "articlePublisher")
  @Mapping(source = "article.category", target = "articleCategory")
  EpisodeDto episodeToEpisodeDto(Episode episode);

  @Mapping(source = "episodeDto.title", target = "title")
  @Mapping(source = "episodeDto.description", target = "description")
  @Mapping(source = "episodeDto.imageUrl", target = "imageUrl")
  @Mapping(source = "episodeDto.scriptUrlPath", target = "scriptUrlPath")
  @Mapping(source = "audioDto.urlPath", target = "audio.urlPath")
  @Mapping(source = "audioDto.duration", target = "audio.duration")
  @Mapping(source = "audioDto.format", target = "audio.format")
  @Mapping(source = "articleDto.title", target = "article.title")
  @Mapping(source = "articleDto.category", target = "article.category")
  @Mapping(source = "articleDto.author", target = "article.author")
  @Mapping(source = "articleDto.publisher", target = "article.publisher")
  @Mapping(source = "articleDto.publicationDate", target = "article.publicationDate")
  @Mapping(source = "articleDto.contentRawUrl", target = "article.contentRawUrl")
  @Mapping(source = "articleDto.scriptUrl", target = "article.scriptUrl")
  Episode sampleEpisodeToEpisode(CreateSampleDto sampleDto);
}
