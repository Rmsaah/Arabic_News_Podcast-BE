package com.shakhbary.arabic_news_podcast.services;

import com.shakhbary.arabic_news_podcast.dtos.EpisodeCreateRequestDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.models.Article;
import com.shakhbary.arabic_news_podcast.models.Audio;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.repositories.ArticleRepository;
import com.shakhbary.arabic_news_podcast.repositories.AudioRepository;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.repositories.RatingRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EpisodeServiceImpl implements EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final RatingRepository ratingRepository;
    private final ArticleRepository articleRepository;
    private final AudioRepository audioRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EpisodeDto> listEpisodes(Pageable pageable) {
        return episodeRepository.findAll(pageable)
                .map(e -> {
                    Double avg = ratingRepository.findAverageRatingForEpisode(e.getId());
                    long count = ratingRepository.countRatingsForEpisode(e.getId());
                    return new EpisodeDto(
                            e.getId(),
                            e.getTitle(),
                            e.getAudio() != null ? e.getAudio().getDuration() : 0L,
                            avg != null ? avg : 0.0,
                            (int) count,
                            e.getImageUrl(),
                            truncate(e.getDescription(), 180),
                            e.getCreationDate()
                    );
                });
    }

    @Override
    @Transactional(readOnly = true)
    public EpisodeDto getEpisode(UUID episodeId) {
        Episode e = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + episodeId));
        Double avg = ratingRepository.findAverageRatingForEpisode(e.getId());
        long count = ratingRepository.countRatingsForEpisode(e.getId());

        return new EpisodeDto(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getScriptUrlPath(),
                e.getAudio() != null ? e.getAudio().getUrlPath() : null,
                e.getAudio() != null ? e.getAudio().getDuration() : 0L,
                avg != null ? avg : 0.0,
                (int) count,
                e.getCreationDate(),
                e.getArticle() != null ? e.getArticle().getId() : null,
                e.getArticle() != null ? e.getArticle().getTitle() : null,
                e.getImageUrl()
        );
    }

    @Override
    @Transactional
    public EpisodeDto createEpisode(EpisodeCreateRequestDto request) {
        Article article = null;
        if (request.getArticleId() != null) {
            article = articleRepository.findById(request.getArticleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Article not found: " + request.getArticleId()));
        }

        Audio audio = null;
        if (request.getAudioUrlPath() != null) {
            audio = new Audio();
            audio.setArticle(article);
            audio.setDuration(request.getDurationSeconds() != null ? request.getDurationSeconds() : 0L);
            audio.setFormat(request.getAudioFormat());
            audio.setUrlPath(request.getAudioUrlPath());
            audio.setCreationDate(OffsetDateTime.now());
            audio = audioRepository.save(audio);
        }

        Episode episode = new Episode();
        episode.setArticle(article);
        episode.setAudio(audio);
        episode.setTitle(request.getTitle());
        episode.setDescription(request.getDescription());
        episode.setScriptUrlPath(request.getScriptUrlPath());
        episode.setImageUrl(request.getImageUrl());
        episode.setCreationDate(OffsetDateTime.now());

        episode = episodeRepository.save(episode);

        return new EpisodeDto(episode.getId(), episode.getCreationDate());
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<EpisodeDto> listDailyEpisodes(int limit) {
        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
        java.time.OffsetDateTime startOfDay = now.toLocalDate().atStartOfDay().atOffset(now.getOffset());
        Page<Episode> page = episodeRepository.findByCreationDateAfterOrderByCreationDateDesc(startOfDay, PageRequest.of(0, limit));
        return page.stream().map(e -> {
            Double avg = ratingRepository.findAverageRatingForEpisode(e.getId());
            long count = ratingRepository.countRatingsForEpisode(e.getId());
            return new EpisodeDto(
                    e.getId(),
                    e.getTitle(),
                    e.getAudio() != null ? e.getAudio().getDuration() : 0L,
                    avg != null ? avg : 0.0,
                    (int) count,
                    e.getImageUrl(),
                    truncate(e.getDescription(), 180),
                    e.getCreationDate()
            );
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EpisodeDto> searchEpisodes(String title, String category, Pageable pageable) {
        Page<Episode> result = episodeRepository.search(
                (title == null || title.isBlank()) ? null : title,
                (category == null || category.isBlank()) ? null : category,
                pageable);
        return result.map(e -> {
            Double avg = ratingRepository.findAverageRatingForEpisode(e.getId());
            long count = ratingRepository.countRatingsForEpisode(e.getId());
            return new EpisodeDto(
                    e.getId(),
                    e.getTitle(),
                    e.getAudio() != null ? e.getAudio().getDuration() : 0L,
                    avg != null ? avg : 0.0,
                    (int) count,
                    e.getImageUrl(),
                    truncate(e.getDescription(), 180),
                    e.getCreationDate()
            );
        });
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}
