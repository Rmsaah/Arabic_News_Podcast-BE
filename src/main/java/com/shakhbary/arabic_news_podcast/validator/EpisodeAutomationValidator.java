package com.shakhbary.arabic_news_podcast.validator;

import com.shakhbary.arabic_news_podcast.dtos.*;
import com.shakhbary.arabic_news_podcast.exceptions.BadRequestException;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class EpisodeAutomationValidator {
  public void validateBulkCreation(List<CreateSampleDto> createSampleDto) {
    if (ObjectUtils.isEmpty(createSampleDto)) {
      throw new BadRequestException("createSampleDto cannot be empty");
    }
    createSampleDto.forEach(this::validateSingleEpisodeCreation);
  }

  public void validateSingleEpisodeCreation(CreateSampleDto createSampleDto) {
    if (ObjectUtils.isEmpty(createSampleDto.getEpisodeDto())) {
      throw new IllegalArgumentException("Episode data is required");
    }
    validateEpisodeData(createSampleDto.getEpisodeDto());

    if (ObjectUtils.isEmpty(createSampleDto.getArticleDto())) {
      throw new IllegalArgumentException("Article data is required");
    }
    validateArticleData(createSampleDto.getArticleDto());

    if (ObjectUtils.isEmpty(createSampleDto.getAudioDto())) {
      throw new IllegalArgumentException("Audio data is required");
    }
    validateAudioData(createSampleDto.getAudioDto());
  }

  private void validateEpisodeData(EpisodeDto episodeDto) {
    if (ObjectUtils.isEmpty(episodeDto.getTitle())) {
      throw new IllegalArgumentException("Episode title is required");
    }
    if (ObjectUtils.isEmpty(episodeDto.getScriptUrlPath())) {
      throw new IllegalArgumentException("Episode scrip URL is required");
    }
  }

  private void validateArticleData(ArticleDto articleDto) {
    if (ObjectUtils.isEmpty(articleDto.getTitle())) {
      throw new IllegalArgumentException("Article title is required");
    }
    if (ObjectUtils.isEmpty(articleDto.getContentRawUrl())) {
      throw new IllegalArgumentException("Article raw content URL is required");
    }
    if (ObjectUtils.isEmpty(articleDto.getScriptUrl())) {
      throw new IllegalArgumentException("Article script URL is required");
    }
    if (ObjectUtils.isEmpty(articleDto.getFetchDate())) {
      throw new IllegalArgumentException("Article fetch date is required");
    }
  }

  private void validateAudioData(AudioDto audioDto) {
    if (ObjectUtils.isEmpty(audioDto.getDuration())) {
      throw new IllegalArgumentException("Audio duration is required");
    }
    if (ObjectUtils.isEmpty(audioDto.getFormat())) {
      throw new IllegalArgumentException("Audio format is required");
    }
    if (ObjectUtils.isEmpty(audioDto.getUrlPath())) {
      throw new IllegalArgumentException("Audio urlPath is required");
    }
  }
}
