package com.shakhbary.arabic_news_podcast.services.Impl;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.repositories.RatingRepository;
import com.shakhbary.arabic_news_podcast.services.EpisodeService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EpisodeServiceImpl implements EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final RatingRepository ratingRepository;

    /**
     * Maps Episode entity to EpisodeDto with all fields populated.
     * This is the single source of truth for episode DTO mapping.
     *
     * @param e Episode entity to map
     * @param truncateDescription If true, truncates description to 180 characters for list views
     * @return Fully populated EpisodeDto with all 15 fields (including Article metadata)
     */
    private EpisodeDto mapToDto(Episode e, boolean truncateDescription) {
        // Fetch ratings data
        Double avg = ratingRepository.findAverageRatingForEpisode(e.getId());
        long count = ratingRepository.countRatingsForEpisode(e.getId());

        // Truncate description if requested (for list views)
        String description = truncateDescription
                ? truncate(e.getDescription(), 180)
                : e.getDescription();

        // Use full 15-parameter constructor to ensure all fields are populated
        return new EpisodeDto(
                e.getId(),
                e.getTitle(),
                description,
                e.getScriptUrlPath(),
                e.getAudio() != null ? e.getAudio().getUrlPath() : null,
                e.getAudio() != null ? e.getAudio().getDuration() : 0L,
                avg != null ? avg : 0.0,
                (int) count,
                e.getCreationDate(),
                e.getArticle() != null ? e.getArticle().getId() : null,
                e.getArticle() != null ? e.getArticle().getTitle() : null,
                e.getArticle() != null ? e.getArticle().getAuthor() : null,
                e.getArticle() != null ? e.getArticle().getPublisher() : null,
                e.getArticle() != null ? e.getArticle().getCategory() : null,
                e.getImageUrl()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EpisodeDto> listEpisodes(Pageable pageable) {
        return episodeRepository.findAll(pageable)
                .map(e -> mapToDto(e, true));
    }

    @Override
    @Transactional(readOnly = true)
    public EpisodeDto getEpisode(UUID episodeId) {
        Episode e = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + episodeId));

        // Validate audio exists (critical for playback)
        if (e.getAudio() == null || e.getAudio().getUrlPath() == null || e.getAudio().getUrlPath().isBlank()) {
            throw new IllegalStateException(
                    "Episode " + episodeId + " has no associated audio file. " +
                            "All episodes must have valid audio for playback."
            );
        }

        return mapToDto(e, false);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<EpisodeDto> listDailyEpisodes(int limit) {
        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
        java.time.OffsetDateTime startOfDay = now.toLocalDate().atStartOfDay().atOffset(now.getOffset());
        Page<Episode> page = episodeRepository.findByCreationDateAfterOrderByCreationDateDesc(startOfDay, PageRequest.of(0, limit));
        return page.stream()
                .map(e -> mapToDto(e, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EpisodeDto> searchEpisodes(String title, String category, Pageable pageable) {
        Page<Episode> result = episodeRepository.search(
                (title == null || title.isBlank()) ? null : title,
                (category == null || category.isBlank()) ? null : category,
                pageable);
        return result.map(e -> mapToDto(e, true));
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}
