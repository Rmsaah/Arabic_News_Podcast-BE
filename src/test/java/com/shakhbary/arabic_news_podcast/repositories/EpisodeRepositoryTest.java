package com.shakhbary.arabic_news_podcast.repositories;

import static org.assertj.core.api.Assertions.*;

import com.shakhbary.arabic_news_podcast.models.Article;
import com.shakhbary.arabic_news_podcast.models.Audio;
import com.shakhbary.arabic_news_podcast.models.Episode;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for EpisodeRepository.
 *
 * Uses @DataJpaTest with H2 in-memory database for isolated testing.
 * Tests custom queries including pagination and search functionality.
 */
@DataJpaTest
@ActiveProfiles("test")  // Add this if using application-test.properties
@TestPropertySource(locations = "file:src/test/resources/application-test.properties")
@DisplayName("EpisodeRepository Integration Tests")
class EpisodeRepositoryTest {

    @Autowired private TestEntityManager entityManager;

    @Autowired private EpisodeRepository episodeRepository;

    private Article testArticle;
    private Audio testAudio;
    private Episode testEpisode;

    @BeforeEach
    void setUp() {
        // Create and persist article
        testArticle = new Article();
        testArticle.setTitle("Test Article");
        testArticle.setCategory("Technology");
        testArticle.setAuthor("Test Author");
        testArticle.setPublisher("Test Publisher");
        testArticle.setPublicationDate(OffsetDateTime.now().minusDays(5));
        testArticle.setContentRawUrl("https://storage.googleapis.com/bucket/article.txt");
        testArticle.setScriptUrl("https://storage.googleapis.com/bucket/script.txt");
        testArticle.setFetchDate(OffsetDateTime.now());
        entityManager.persist(testArticle);

        // Create and persist audio
        testAudio = new Audio();
        testAudio.setArticle(testArticle);
        testAudio.setDuration(180L);
        testAudio.setFormat("mp3");
        testAudio.setUrlPath("https://storage.googleapis.com/bucket/audio.mp3");
        testAudio.setCreationDate(OffsetDateTime.now().minusDays(2));
        entityManager.persist(testAudio);

        // Create and persist episode
        testEpisode = new Episode();
        testEpisode.setArticle(testArticle);
        testEpisode.setAudio(testAudio);
        testEpisode.setTitle("Test Episode");
        testEpisode.setDescription("Test description");
        testEpisode.setScriptUrlPath("https://storage.googleapis.com/bucket/script.txt");
        testEpisode.setImageUrl("https://storage.googleapis.com/bucket/image.jpg");
        testEpisode.setCreationDate(OffsetDateTime.now().minusDays(1));
        entityManager.persistAndFlush(testEpisode);
    }

    @Test
    @DisplayName("Should find episodes created after specific date")
    void testFindByCreationDateAfter_Success() {
        // Arrange
        OffsetDateTime twoDaysAgo = OffsetDateTime.now().minusDays(2);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> result =
                episodeRepository.findByCreationDateAfterOrderByCreationDateDesc(twoDaysAgo, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Episode");
    }

    @Test
    @DisplayName("Should return empty page when no episodes after date")
    void testFindByCreationDateAfter_NoResults() {
        // Arrange
        OffsetDateTime future = OffsetDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> result =
                episodeRepository.findByCreationDateAfterOrderByCreationDateDesc(future, pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should search episodes by title")
    void testSearch_ByTitle() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> result = episodeRepository.search("Test", null, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Episode");
    }

    @Test
    @DisplayName("Should search episodes by partial title match")
    void testSearch_PartialTitle() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> result = episodeRepository.search("Epi", null, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should search episodes by category")
    void testSearch_ByCategory() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> result = episodeRepository.search(null, "Technology", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getArticle().getCategory()).isEqualTo("Technology");
    }

    @Test
    @DisplayName("Should search episodes by title and category")
    void testSearch_ByTitleAndCategory() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> result = episodeRepository.search("Test", "Technology", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should return empty when search finds no matches")
    void testSearch_NoMatches() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> result = episodeRepository.search("NonExistent", null, pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should handle case-insensitive title search")
    void testSearch_CaseInsensitive() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> lowercase = episodeRepository.search("test episode", null, pageable);
        Page<Episode> uppercase = episodeRepository.search("TEST EPISODE", null, pageable);

        // Assert
        assertThat(lowercase.getContent()).hasSize(1);
        assertThat(uppercase.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle case-insensitive category search")
    void testSearch_CategoryCaseInsensitive() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> lowercase = episodeRepository.search(null, "technology", pageable);
        Page<Episode> uppercase = episodeRepository.search(null, "TECHNOLOGY", pageable);

        // Assert
        assertThat(lowercase.getContent()).hasSize(1);
        assertThat(uppercase.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should return all episodes when both search params are null")
    void testSearch_AllEpisodes() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> result = episodeRepository.search(null, null, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testFindByCreationDateAfter_Pagination() {
        // Arrange - Create multiple episodes
        for (int i = 0; i < 5; i++) {
            Episode ep = new Episode();
            ep.setArticle(testArticle);
            ep.setAudio(testAudio);
            ep.setTitle("Episode " + i);
            ep.setDescription("Description " + i);
            ep.setScriptUrlPath("https://storage.googleapis.com/bucket/script.txt");
            ep.setCreationDate(OffsetDateTime.now().minusHours(i));
            entityManager.persist(ep);
        }
        entityManager.flush();

        OffsetDateTime oneDayAgo = OffsetDateTime.now().minusDays(1);
        Pageable firstPage = PageRequest.of(0, 3);
        Pageable secondPage = PageRequest.of(1, 3);

        // Act
        Page<Episode> page1 =
                episodeRepository.findByCreationDateAfterOrderByCreationDateDesc(oneDayAgo, firstPage);
        Page<Episode> page2 =
                episodeRepository.findByCreationDateAfterOrderByCreationDateDesc(oneDayAgo, secondPage);

        // Assert
        assertThat(page1.getContent()).hasSize(3);
        assertThat(page2.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(page1.getTotalElements()).isEqualTo(6); // 5 + 1 from setUp
    }

    @Test
    @DisplayName("Should order episodes by creation date descending")
    void testFindByCreationDateAfter_OrderDescending() {
        // Arrange - Create episodes with different dates
        Episode older = new Episode();
        older.setArticle(testArticle);
        older.setAudio(testAudio);
        older.setTitle("Older Episode");
        older.setDescription("Older");
        older.setScriptUrlPath("https://storage.googleapis.com/bucket/script.txt");
        older.setCreationDate(OffsetDateTime.now().minusDays(10));
        entityManager.persist(older);

        Episode newer = new Episode();
        newer.setArticle(testArticle);
        newer.setAudio(testAudio);
        newer.setTitle("Newer Episode");
        newer.setDescription("Newer");
        newer.setScriptUrlPath("https://storage.googleapis.com/bucket/script.txt");
        newer.setCreationDate(OffsetDateTime.now().minusHours(1));
        entityManager.persistAndFlush(newer);

        OffsetDateTime longAgo = OffsetDateTime.now().minusDays(15);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Episode> result =
                episodeRepository.findByCreationDateAfterOrderByCreationDateDesc(longAgo, pageable);

        // Assert
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(3);
        // First episode should be the newest
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Newer Episode");
    }

    @Test
    @DisplayName("Should save and retrieve episode with all relationships")
    void testSaveEpisode_WithRelationships() {
        // Arrange
        Episode newEpisode = new Episode();
        newEpisode.setArticle(testArticle);
        newEpisode.setAudio(testAudio);
        newEpisode.setTitle("New Episode");
        newEpisode.setDescription("New description");
        newEpisode.setScriptUrlPath("https://storage.googleapis.com/bucket/new-script.txt");
        newEpisode.setImageUrl("https://storage.googleapis.com/bucket/new-image.jpg");
        newEpisode.setCreationDate(OffsetDateTime.now());

        // Act
        Episode saved = episodeRepository.save(newEpisode);
        entityManager.flush();

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getArticle()).isNotNull();
        assertThat(saved.getAudio()).isNotNull();

        // Verify retrieval
        Episode found = episodeRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("New Episode");
    }

    @Test
    @DisplayName("Should delete episode by id")
    void testDeleteEpisode() {
        // Arrange
        Episode episode = episodeRepository.findAll().get(0);

        // Act
        episodeRepository.deleteById(episode.getId());
        entityManager.flush();

        // Assert
        assertThat(episodeRepository.findById(episode.getId())).isEmpty();
    }
}
