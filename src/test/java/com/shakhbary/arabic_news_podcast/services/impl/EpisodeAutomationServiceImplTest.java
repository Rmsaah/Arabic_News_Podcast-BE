package com.shakhbary.arabic_news_podcast.services.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeDto;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeJsonDto;
import com.shakhbary.arabic_news_podcast.models.Article;
import com.shakhbary.arabic_news_podcast.models.Audio;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.repositories.ArticleRepository;
import com.shakhbary.arabic_news_podcast.repositories.AudioRepository;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.shakhbary.arabic_news_podcast.services.Impl.EpisodeAutomationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/**
 * Unit tests for EpisodeAutomationServiceImpl.
 *
 * Tests episode automation service logic including:
 * - JSON parsing and deserialization
 * - Episode creation with nested entities (Article, Audio, Episode)
 * - Validation of required fields
 * - Error handling for malformed input data
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")  // Add this if using application-test.properties
@DisplayName("EpisodeAutomationService Unit Tests")
class EpisodeAutomationServiceImplTest {

    @Mock private EpisodeRepository episodeRepository;

    @Mock private ArticleRepository articleRepository;

    @Mock private AudioRepository audioRepository;

    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private EpisodeAutomationServiceImpl episodeAutomationService;

    private EpisodeJsonDto validEpisodeJsonDto;
    private Article savedArticle;
    private Audio savedAudio;
    private Episode savedEpisode;

    @BeforeEach
    void setUp() {
        // Prepare valid nested JSON DTO
        EpisodeJsonDto.ArticleData articleData = new EpisodeJsonDto.ArticleData();
        articleData.setTitle("Test Article Title");
        articleData.setCategory("Technology");
        articleData.setAuthor("Test Author");
        articleData.setPublisher("Test Publisher");
        articleData.setPublicationDate("2024-12-01T10:00:00Z");
        articleData.setContentRawUrl(
                "https://storage.googleapis.com/bucket/articles/raw-content.txt");
        articleData.setScriptUrl("https://storage.googleapis.com/bucket/articles/script.txt");

        EpisodeJsonDto.AudioData audioData = new EpisodeJsonDto.AudioData();
        audioData.setDuration(180L); // 3 minutes
        audioData.setFormat("mp3");
        audioData.setUrlPath("https://storage.googleapis.com/bucket/audio/test-audio.mp3");

        EpisodeJsonDto.EpisodeData episodeData = new EpisodeJsonDto.EpisodeData();
        episodeData.setTitle("Test Episode Title");
        episodeData.setDescription("Test episode description");
        episodeData.setScriptUrlPath("https://storage.googleapis.com/bucket/scripts/episode-script.txt");
        episodeData.setImageUrl("https://storage.googleapis.com/bucket/images/episode-cover.jpg");

        validEpisodeJsonDto = new EpisodeJsonDto();
        validEpisodeJsonDto.setArticle(articleData);
        validEpisodeJsonDto.setAudio(audioData);
        validEpisodeJsonDto.setEpisode(episodeData);

        // Prepare saved entities
        savedArticle = new Article();
        savedArticle.setId(UUID.randomUUID());
        savedArticle.setTitle("Test Article Title");
        savedArticle.setCategory("Technology");
        savedArticle.setAuthor("Test Author");
        savedArticle.setPublisher("Test Publisher");
        savedArticle.setPublicationDate(OffsetDateTime.parse("2024-12-01T10:00:00Z"));
        savedArticle.setContentRawUrl(
                "https://storage.googleapis.com/bucket/articles/raw-content.txt");
        savedArticle.setScriptUrl("https://storage.googleapis.com/bucket/articles/script.txt");
        savedArticle.setFetchDate(OffsetDateTime.now());

        savedAudio = new Audio();
        savedAudio.setId(UUID.randomUUID());
        savedAudio.setArticle(savedArticle);
        savedAudio.setDuration(180L);
        savedAudio.setFormat("mp3");
        savedAudio.setUrlPath("https://storage.googleapis.com/bucket/audio/test-audio.mp3");
        savedAudio.setCreationDate(OffsetDateTime.now());

        savedEpisode = new Episode();
        savedEpisode.setId(UUID.randomUUID());
        savedEpisode.setArticle(savedArticle);
        savedEpisode.setAudio(savedAudio);
        savedEpisode.setTitle("Test Episode Title");
        savedEpisode.setDescription("Test episode description");
        savedEpisode.setScriptUrlPath(
                "https://storage.googleapis.com/bucket/scripts/episode-script.txt");
        savedEpisode.setImageUrl("https://storage.googleapis.com/bucket/images/episode-cover.jpg");
        savedEpisode.setCreationDate(OffsetDateTime.now());
    }

    @Test
    @DisplayName("Should successfully process valid episode JSON")
    void testProcessEpisodeFromJson_Success() throws Exception {
        // Arrange
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(audioRepository.save(any(Audio.class))).thenReturn(savedAudio);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // Act
        EpisodeDto result = episodeAutomationService.processEpisodeFromJson(episodeJson);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Episode Title");
        assertThat(result.getDescription()).isEqualTo("Test episode description");
        assertThat(result.getDurationSeconds()).isEqualTo(180L);
        assertThat(result.getArticleTitle()).isEqualTo("Test Article Title");
        assertThat(result.getArticleAuthor()).isEqualTo("Test Author");
        assertThat(result.getArticlePublisher()).isEqualTo("Test Publisher");
        assertThat(result.getArticleCategory()).isEqualTo("Technology");

        // Verify all entities were saved
        verify(articleRepository).save(any(Article.class));
        verify(audioRepository).save(any(Audio.class));
        verify(episodeRepository).save(any(Episode.class));
    }

    @Test
    @DisplayName("Should throw exception when article data is missing")
    void testProcessEpisodeFromJson_MissingArticle() throws Exception {
        // Arrange
        validEpisodeJsonDto.setArticle(null);
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        // Act & Assert
        assertThatThrownBy(() -> episodeAutomationService.processEpisodeFromJson(episodeJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Article data is required");

        // Verify no entities were saved
        verify(articleRepository, never()).save(any(Article.class));
        verify(audioRepository, never()).save(any(Audio.class));
        verify(episodeRepository, never()).save(any(Episode.class));
    }

    @Test
    @DisplayName("Should throw exception when audio data is missing")
    void testProcessEpisodeFromJson_MissingAudio() throws Exception {
        // Arrange
        validEpisodeJsonDto.setAudio(null);
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        // Act & Assert
        assertThatThrownBy(() -> episodeAutomationService.processEpisodeFromJson(episodeJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Audio data is required");

        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    @DisplayName("Should throw exception when episode data is missing")
    void testProcessEpisodeFromJson_MissingEpisode() throws Exception {
        // Arrange
        validEpisodeJsonDto.setEpisode(null);
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        // Act & Assert
        assertThatThrownBy(() -> episodeAutomationService.processEpisodeFromJson(episodeJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Episode data is required");
    }

    @Test
    @DisplayName("Should throw exception when article title is blank")
    void testProcessEpisodeFromJson_BlankArticleTitle() throws Exception {
        // Arrange
        validEpisodeJsonDto.getArticle().setTitle("");
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        // Act & Assert
        assertThatThrownBy(() -> episodeAutomationService.processEpisodeFromJson(episodeJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Article title is required");
    }

    @Test
    @DisplayName("Should throw exception when audio urlPath is blank")
    void testProcessEpisodeFromJson_BlankAudioUrl() throws Exception {
        // Arrange
        validEpisodeJsonDto.getAudio().setUrlPath("");
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        // Act & Assert
        assertThatThrownBy(() -> episodeAutomationService.processEpisodeFromJson(episodeJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Audio urlPath is required");
    }

    @Test
    @DisplayName("Should throw exception when episode title is blank")
    void testProcessEpisodeFromJson_BlankEpisodeTitle() throws Exception {
        // Arrange
        validEpisodeJsonDto.getEpisode().setTitle("");
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        // Act & Assert
        assertThatThrownBy(() -> episodeAutomationService.processEpisodeFromJson(episodeJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Episode title is required");
    }

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    void testProcessEpisodeFromJson_MalformedJson() {
        // Arrange
        String malformedJson = "{invalid json}";

        // Act & Assert
        assertThatThrownBy(() -> episodeAutomationService.processEpisodeFromJson(malformedJson))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process episode from JSON");
    }

    @Test
    @DisplayName("Should create article with all fields properly set")
    void testProcessEpisodeFromJson_ArticleFieldsValidation() throws Exception {
        // Arrange
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(audioRepository.save(any(Audio.class))).thenReturn(savedAudio);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // Act
        episodeAutomationService.processEpisodeFromJson(episodeJson);

        // Assert
        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(articleCaptor.capture());
        Article capturedArticle = articleCaptor.getValue();

        assertThat(capturedArticle.getTitle()).isEqualTo("Test Article Title");
        assertThat(capturedArticle.getCategory()).isEqualTo("Technology");
        assertThat(capturedArticle.getAuthor()).isEqualTo("Test Author");
        assertThat(capturedArticle.getPublisher()).isEqualTo("Test Publisher");
        assertThat(capturedArticle.getPublicationDate()).isNotNull();
        assertThat(capturedArticle.getContentRawUrl())
                .isEqualTo("https://storage.googleapis.com/bucket/articles/raw-content.txt");
        assertThat(capturedArticle.getScriptUrl())
                .isEqualTo("https://storage.googleapis.com/bucket/articles/script.txt");
        assertThat(capturedArticle.getFetchDate()).isNotNull();
    }

    @Test
    @DisplayName("Should create audio with correct article reference")
    void testProcessEpisodeFromJson_AudioArticleReference() throws Exception {
        // Arrange
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(audioRepository.save(any(Audio.class))).thenReturn(savedAudio);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // Act
        episodeAutomationService.processEpisodeFromJson(episodeJson);

        // Assert
        ArgumentCaptor<Audio> audioCaptor = ArgumentCaptor.forClass(Audio.class);
        verify(audioRepository).save(audioCaptor.capture());
        Audio capturedAudio = audioCaptor.getValue();

        assertThat(capturedAudio.getArticle()).isEqualTo(savedArticle);
        assertThat(capturedAudio.getDuration()).isEqualTo(180L);
        assertThat(capturedAudio.getFormat()).isEqualTo("mp3");
        assertThat(capturedAudio.getUrlPath())
                .isEqualTo("https://storage.googleapis.com/bucket/audio/test-audio.mp3");
        assertThat(capturedAudio.getCreationDate()).isNotNull();
    }

    @Test
    @DisplayName("Should create episode with correct article and audio references")
    void testProcessEpisodeFromJson_EpisodeReferences() throws Exception {
        // Arrange
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(audioRepository.save(any(Audio.class))).thenReturn(savedAudio);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // Act
        episodeAutomationService.processEpisodeFromJson(episodeJson);

        // Assert
        ArgumentCaptor<Episode> episodeCaptor = ArgumentCaptor.forClass(Episode.class);
        verify(episodeRepository).save(episodeCaptor.capture());
        Episode capturedEpisode = episodeCaptor.getValue();

        assertThat(capturedEpisode.getArticle()).isEqualTo(savedArticle);
        assertThat(capturedEpisode.getAudio()).isEqualTo(savedAudio);
        assertThat(capturedEpisode.getTitle()).isEqualTo("Test Episode Title");
        assertThat(capturedEpisode.getDescription()).isEqualTo("Test episode description");
        assertThat(capturedEpisode.getCreationDate()).isNotNull();
    }

    @Test
    @DisplayName("Should use article scriptUrl when episode scriptUrlPath is not provided")
    void testProcessEpisodeFromJson_FallbackScriptUrl() throws Exception {
        // Arrange
        validEpisodeJsonDto.getEpisode().setScriptUrlPath(null);
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(audioRepository.save(any(Audio.class))).thenReturn(savedAudio);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // Act
        episodeAutomationService.processEpisodeFromJson(episodeJson);

        // Assert
        ArgumentCaptor<Episode> episodeCaptor = ArgumentCaptor.forClass(Episode.class);
        verify(episodeRepository).save(episodeCaptor.capture());
        Episode capturedEpisode = episodeCaptor.getValue();

        // Should fall back to article's scriptUrl
        assertThat(capturedEpisode.getScriptUrlPath()).isEqualTo(savedArticle.getScriptUrl());
    }

    @Test
    @DisplayName("Should handle missing optional fields gracefully")
    void testProcessEpisodeFromJson_OptionalFields() throws Exception {
        // Arrange
        validEpisodeJsonDto.getArticle().setCategory(null);
        validEpisodeJsonDto.getArticle().setAuthor(null);
        validEpisodeJsonDto.getArticle().setPublisher(null);
        validEpisodeJsonDto.getAudio().setFormat(null);
        validEpisodeJsonDto.getEpisode().setDescription(null);
        validEpisodeJsonDto.getEpisode().setImageUrl(null);

        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(audioRepository.save(any(Audio.class))).thenReturn(savedAudio);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // Act
        EpisodeDto result = episodeAutomationService.processEpisodeFromJson(episodeJson);

        // Assert - should not throw exception
        assertThat(result).isNotNull();
        verify(articleRepository).save(any(Article.class));
        verify(audioRepository).save(any(Audio.class));
        verify(episodeRepository).save(any(Episode.class));
    }

    @Test
    @DisplayName("Should handle audio duration as zero when not provided")
    void testProcessEpisodeFromJson_MissingDuration() throws Exception {
        // Arrange
        validEpisodeJsonDto.getAudio().setDuration(null);
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(audioRepository.save(any(Audio.class))).thenReturn(savedAudio);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // Act
        episodeAutomationService.processEpisodeFromJson(episodeJson);

        // Assert
        ArgumentCaptor<Audio> audioCaptor = ArgumentCaptor.forClass(Audio.class);
        verify(audioRepository).save(audioCaptor.capture());
        Audio capturedAudio = audioCaptor.getValue();

        assertThat(capturedAudio.getDuration()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should parse and store publication date correctly")
    void testProcessEpisodeFromJson_PublicationDate() throws Exception {
        // Arrange
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(audioRepository.save(any(Audio.class))).thenReturn(savedAudio);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // Act
        episodeAutomationService.processEpisodeFromJson(episodeJson);

        // Assert
        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(articleCaptor.capture());
        Article capturedArticle = articleCaptor.getValue();

        assertThat(capturedArticle.getPublicationDate()).isNotNull();
        assertThat(capturedArticle.getPublicationDate().toString()).contains("2024-12-01");
    }

    @Test
    @DisplayName("Should handle missing publication date gracefully")
    void testProcessEpisodeFromJson_MissingPublicationDate() throws Exception {
        // Arrange
        validEpisodeJsonDto.getArticle().setPublicationDate(null);
        String episodeJson = objectMapper.writeValueAsString(validEpisodeJsonDto);

        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(audioRepository.save(any(Audio.class))).thenReturn(savedAudio);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // Act & Assert - should not throw exception
        assertThatCode(() -> episodeAutomationService.processEpisodeFromJson(episodeJson))
                .doesNotThrowAnyException();
    }
}
