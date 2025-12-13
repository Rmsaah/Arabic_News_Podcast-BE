package com.shakhbary.arabic_news_podcast.services.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shakhbary.arabic_news_podcast.dtos.CreateSampleDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.mappers.EpisodeMapper;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.repositories.ArticleRepository;
import com.shakhbary.arabic_news_podcast.repositories.AudioRepository;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.services.EpisodeAutomationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpisodeAutomationServiceImpl implements EpisodeAutomationService {

  private final EpisodeRepository episodeRepository;
  private final ArticleRepository articleRepository;
  private final AudioRepository audioRepository;
  private final ObjectMapper objectMapper;
  private final EpisodeMapper episodeMapper;
  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${agent.base.url}")
  private String AGENT_BASE_URL;

  /**
   * Automated pipeline: Scrape news → Process all → Save to database Calls Python agent to do all
   * the heavy lifting
   */
  @Transactional
  @Scheduled(cron = "0 0 9 * * *") // CRON expression for 9:00 AM daily
  public List<EpisodeDto> automatedDailyPipeline() {
    try {
      log.info("Starting automated daily pipeline...");

      // Call Python agent
      String url = AGENT_BASE_URL + "/scrape-and-process-all";

      log.info("Calling Python agent: {}", url);
      ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        Map<String, Object> body = response.getBody();
        Boolean success = (Boolean) body.get("success");

        if (Boolean.TRUE.equals(success)) {
          List<CreateSampleDto> episodesFromAgent =
              objectMapper.convertValue(body.get("episodes"), new TypeReference<>() {});

          log.info("Received {} episodes from agent", episodesFromAgent.size());

          List<EpisodeDto> savedEpisodes = new ArrayList<>();

          // Process each episode
          for (CreateSampleDto episodeDto : episodesFromAgent) {
            try {
              EpisodeDto dto = createEpisode(episodeDto);
              savedEpisodes.add(dto);
              log.info("Saved episode: {}", dto.getTitle());
            } catch (Exception e) {
              log.error("Error saving episode: {}", e.getMessage());
            }
          }

          log.info(
              "Pipeline complete: {}/{} episodes saved",
              savedEpisodes.size(),
              episodesFromAgent.size());

          return savedEpisodes;
        } else {
          log.error("Agent returned failure");
          throw new RuntimeException("Agent processing failed");
        }
      } else {
        log.error("Agent returned non-200 status");
        throw new RuntimeException("Agent not responding");
      }

    } catch (Exception e) {
      log.error("Error in automated pipeline: {}", e.getMessage());
      throw new RuntimeException("Automated pipeline failed", e);
    }
  }

  @Override
  @Transactional
  public List<EpisodeDto> createBulkEpisodes(List<CreateSampleDto> createSampleDtoList) {
    List<EpisodeDto> savedEpisodes = new ArrayList<>();
    createSampleDtoList.forEach(dto -> savedEpisodes.add(createEpisode(dto)));
    return savedEpisodes;
  }

  @Override
  @Transactional
  public EpisodeDto createEpisode(CreateSampleDto createSampleDto) {
    Episode episode = episodeMapper.sampleEpisodeToEpisode(createSampleDto);
    episode.setArticle(articleRepository.save(episode.getArticle()));
    episode.getAudio().setEpisode(episode);
    episode.getAudio().setArticle(episode.getArticle());
    episode.setAudio(audioRepository.save(episode.getAudio()));
    episode = episodeRepository.save(episode);
    return episodeMapper.episodeToEpisodeDto(episode);
  }
}
