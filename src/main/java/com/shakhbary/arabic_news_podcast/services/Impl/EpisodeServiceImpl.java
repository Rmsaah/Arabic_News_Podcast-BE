package com.shakhbary.arabic_news_podcast.services.Impl;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.mappers.EpisodeMapper;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.repositories.RatingRepository;
import com.shakhbary.arabic_news_podcast.services.EpisodeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpisodeServiceImpl implements EpisodeService {

  private final EpisodeRepository episodeRepository;
  private final RatingRepository ratingRepository;
  private final EpisodeMapper episodeMapper;

  /**
   * Maps Episode entity to EpisodeDto with all fields populated. This is the single source of truth
   * for episode DTO mapping.
   *
   * @param episode Episode entity to map
   * @param truncateDescription If true, truncates description to 180 characters for list views
   * @return Fully populated EpisodeDto with all 15 fields (including Article metadata)
   */
  private EpisodeDto mapToDto(Episode episode, boolean truncateDescription) {
    EpisodeDto episodeDto = episodeMapper.episodeToEpisodeDto(episode);

    // Fetch ratings data
    Double avg = ratingRepository.findAverageRatingForEpisode(episode.getId());
    long count = ratingRepository.countRatingsForEpisode(episode.getId());

    episodeDto.setAverageRating(avg == null ? 0.0 : avg);
    episodeDto.setRatingCount((int) count);

    // Truncate description if requested (for list views)
    String description =
        truncateDescription ? truncate(episode.getDescription(), 180) : episode.getDescription();
    episodeDto.setDescription(description);

    return episodeDto;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<EpisodeDto> listEpisodes(Pageable pageable) {
    log.info(
        "Listing episodes with pageable: page={}, size={}",
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<Episode> episodes = episodeRepository.findAll(pageable);
    log.info("Retrieved {} episodes from database", episodes.getTotalElements());

    return episodeRepository.findAll(pageable).map(e -> mapToDto(e, true));
  }

  @Override
  @Transactional(readOnly = true)
  public EpisodeDto getEpisode(UUID episodeId) {
    log.info("Getting episode with id: {}", episodeId);

    Episode e =
        episodeRepository
            .findById(episodeId)
            .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + episodeId));

    log.info("Found episode: id={}, title={}", e.getId(), e.getTitle());

    // Validate audio exists (critical for playback)
    if (e.getAudio() == null
        || e.getAudio().getUrlPath() == null
        || e.getAudio().getUrlPath().isBlank()) {
      log.error("Episode {} has no associated audio file", episodeId);
      throw new IllegalStateException(
          "Episode "
              + episodeId
              + " has no associated audio file. "
              + "All episodes must have valid audio for playback.");
    }

    log.info("Episode {} has valid audio file: {}", episodeId, e.getAudio().getUrlPath());
    return mapToDto(e, false);
  }

  @Override
  @Transactional(readOnly = true)
  public java.util.List<EpisodeDto> listDailyEpisodes(int limit) {
    log.info("Getting {} episodes from today", limit);

    java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
    java.time.OffsetDateTime startOfDay =
        now.toLocalDate().atStartOfDay().atOffset(now.getOffset());

    log.info("Fetching episodes created after: {}", startOfDay);

    Page<Episode> page =
        episodeRepository.findByCreationDateAfterOrderByCreationDateDesc(
            startOfDay, PageRequest.of(0, limit));

    log.info("Retrieved {} daily episodes", page.getNumberOfElements());

    return page.stream().map(e -> mapToDto(e, true)).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<EpisodeDto> searchEpisodes(String title, String category, Pageable pageable) {

    log.info(
        "Searching episodes with title='{}', category='{}', page={}, size={}",
        title,
        category,
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<Episode> result =
        episodeRepository.search(
            (title == null || title.isBlank()) ? null : title,
            (category == null || category.isBlank()) ? null : category,
            pageable);

    log.info(
        "Search returned {} episodes (total: {})",
        result.getNumberOfElements(),
        result.getTotalElements());

    return result.map(e -> mapToDto(e, true));
  }

  private String truncate(String s, int max) {
    if (s == null) return null;
    if (s.length() <= max) return s;
    return s.substring(0, max - 3) + "...";
  }
}
