package com.shakhbary.arabic_news_podcast.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shakhbary.arabic_news_podcast.dtos.EpisodeJsonDto;
import com.shakhbary.arabic_news_podcast.models.Article;
import com.shakhbary.arabic_news_podcast.models.Audio;
import com.shakhbary.arabic_news_podcast.models.Episode;
import com.shakhbary.arabic_news_podcast.models.Role;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.ArticleRepository;
import com.shakhbary.arabic_news_podcast.repositories.AudioRepository;
import com.shakhbary.arabic_news_podcast.repositories.EpisodeRepository;
import com.shakhbary.arabic_news_podcast.repositories.RoleRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for EpisodeAutomationController.
 *
 * Tests admin automation endpoints with MockMvc:
 * - JSON file upload and processing
 * - Single episode JSON processing
 * - Authentication and authorization
 * - File validation and error handling
 */
@SpringBootTest
@ActiveProfiles("test")  // Add this if using application-test.properties
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:src/test/resources/application-test.properties")
@Transactional
@DisplayName("EpisodeAutomationController Integration Tests")
class EpisodeAutomationControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private EpisodeRepository episodeRepository;

    @Autowired private ArticleRepository articleRepository;

    @Autowired private AudioRepository audioRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Clean database
        episodeRepository.deleteAll();
        audioRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        roleRepository.save(adminRole);

        userRole = new Role();
        userRole.setName("ROLE_USER");
        roleRepository.save(userRole);

        // Create admin user
        adminUser = createUser("admin", "admin@example.com", "admin123", Set.of(adminRole, userRole));
        userRepository.save(adminUser);

        // Create regular user
        regularUser = createUser("user", "user@example.com", "user123", Set.of(userRole));
        userRepository.save(regularUser);
    }

    @Test
    @DisplayName("Should successfully process episode JSON with admin authentication")
    void testProcessEpisode_Success() throws Exception {
        // Arrange
        EpisodeJsonDto episodeJson = createValidEpisodeJsonDto();
        String jsonContent = objectMapper.writeValueAsString(episodeJson);

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("admin", "admin123"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.episodes").isArray())
                .andExpect(jsonPath("$.episodes[0].title").value("Test Episode"))
                .andExpect(jsonPath("$.episodes[0].articleTitle").value("Test Article"))
                .andExpect(jsonPath("$.episodes[0].articleAuthor").value("Test Author"))
                .andExpect(jsonPath("$.episodes[0].durationSeconds").value(180));

        // Verify entities were created
        assertThat(episodeRepository.count()).isEqualTo(1);
        assertThat(articleRepository.count()).isEqualTo(1);
        assertThat(audioRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should reject episode processing without authentication")
    void testProcessEpisode_NoAuth() throws Exception {
        // Arrange
        EpisodeJsonDto episodeJson = createValidEpisodeJsonDto();
        String jsonContent = objectMapper.writeValueAsString(episodeJson);

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent))
                .andExpect(status().isUnauthorized());

        // Verify no entities were created
        assertThat(episodeRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reject episode processing for non-admin user")
    void testProcessEpisode_NonAdmin() throws Exception {
        // Arrange
        EpisodeJsonDto episodeJson = createValidEpisodeJsonDto();
        String jsonContent = objectMapper.writeValueAsString(episodeJson);

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("user", "user123"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent))
                .andExpect(status().isForbidden());

        // Verify no entities were created
        assertThat(episodeRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reject empty JSON body")
    void testProcessEpisode_EmptyBody() throws Exception {
        // Act & Assert
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("admin", "admin123"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Episode JSON cannot be empty"));
    }

    @Test
    @DisplayName("Should reject malformed JSON")
    void testProcessEpisode_MalformedJson() throws Exception {
        // Arrange
        String malformedJson = "{invalid json}";

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("admin", "admin123"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(malformedJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should reject JSON with missing required fields")
    void testProcessEpisode_MissingFields() throws Exception {
        // Arrange - Episode without audio data
        EpisodeJsonDto incompleteJson = createValidEpisodeJsonDto();
        incompleteJson.setAudio(null);
        String jsonContent = objectMapper.writeValueAsString(incompleteJson);

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("admin", "admin123"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should successfully upload and process JSON file")
    void testUploadJsonFile_Success() throws Exception {
        // Arrange
        EpisodeJsonDto episode1 = createValidEpisodeJsonDto();
        episode1.getEpisode().setTitle("Episode 1");

        EpisodeJsonDto episode2 = createValidEpisodeJsonDto();
        episode2.getEpisode().setTitle("Episode 2");
        episode2.getArticle().setTitle("Article 2");
        episode2.getAudio().setUrlPath("https://storage.googleapis.com/bucket/audio2.mp3");

        String jsonContent =
                objectMapper.writeValueAsString(new EpisodeJsonDto[] {episode1, episode2});

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "episodes.json",
                        MediaType.APPLICATION_JSON_VALUE,
                        jsonContent.getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        mockMvc
                .perform(
                        multipart("/api/admin/automation/upload-json")
                                .file(file)
                                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.episodes").isArray())
                .andExpect(jsonPath("$.episodes.length()").value(2));

        // Verify entities were created
        assertThat(episodeRepository.count()).isEqualTo(2);
        assertThat(articleRepository.count()).isEqualTo(2);
        assertThat(audioRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should reject file upload without authentication")
    void testUploadJsonFile_NoAuth() throws Exception {
        // Arrange
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "episodes.json",
                        MediaType.APPLICATION_JSON_VALUE,
                        "[]".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        mockMvc
                .perform(multipart("/api/admin/automation/upload-json").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject file upload for non-admin user")
    void testUploadJsonFile_NonAdmin() throws Exception {
        // Arrange
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "episodes.json",
                        MediaType.APPLICATION_JSON_VALUE,
                        "[]".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        mockMvc
                .perform(
                        multipart("/api/admin/automation/upload-json")
                                .file(file)
                                .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject empty file upload")
    void testUploadJsonFile_EmptyFile() throws Exception {
        // Arrange
        MockMultipartFile emptyFile =
                new MockMultipartFile(
                        "file", "episodes.json", MediaType.APPLICATION_JSON_VALUE, new byte[0]);

        // Act & Assert
        mockMvc
                .perform(
                        multipart("/api/admin/automation/upload-json")
                                .file(emptyFile)
                                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("File cannot be empty"));
    }

    @Test
    @DisplayName("Should reject non-JSON file extension")
    void testUploadJsonFile_WrongExtension() throws Exception {
        // Arrange
        MockMultipartFile txtFile =
                new MockMultipartFile(
                        "file",
                        "episodes.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "some content".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        mockMvc
                .perform(
                        multipart("/api/admin/automation/upload-json")
                                .file(txtFile)
                                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("File must be a JSON file (.json extension required)"));
    }

    @Test
    @DisplayName("Should reject file exceeding size limit")
    void testUploadJsonFile_FileTooLarge() throws Exception {
        // Arrange - Create file > 10MB
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile largeFile =
                new MockMultipartFile(
                        "file", "large.json", MediaType.APPLICATION_JSON_VALUE, largeContent);

        // Act & Assert
        mockMvc
                .perform(
                        multipart("/api/admin/automation/upload-json")
                                .file(largeFile)
                                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should handle missing file parameter")
    void testUploadJsonFile_MissingFile() throws Exception {
        // Act & Assert
        mockMvc
                .perform(
                        multipart("/api/admin/automation/upload-json").with(httpBasic("admin", "admin123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create episode with all nested entity relationships")
    void testProcessEpisode_EntityRelationships() throws Exception {
        // Arrange
        EpisodeJsonDto episodeJson = createValidEpisodeJsonDto();
        String jsonContent = objectMapper.writeValueAsString(episodeJson);

        // Act
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("admin", "admin123"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent))
                .andExpect(status().isCreated());

        // Assert - Verify relationships
        Episode episode = episodeRepository.findAll().get(0);
        assertThat(episode.getArticle()).isNotNull();
        assertThat(episode.getAudio()).isNotNull();
        assertThat(episode.getAudio().getArticle()).isEqualTo(episode.getArticle());
    }

    @Test
    @DisplayName("Should process multiple episodes independently")
    void testUploadJsonFile_MultipleEpisodes() throws Exception {
        // Arrange - Create 3 different episodes
        EpisodeJsonDto[] episodes = new EpisodeJsonDto[3];
        for (int i = 0; i < 3; i++) {
            episodes[i] = createValidEpisodeJsonDto();
            episodes[i].getEpisode().setTitle("Episode " + (i + 1));
            episodes[i].getArticle().setTitle("Article " + (i + 1));
            episodes[i]
                    .getAudio()
                    .setUrlPath("https://storage.googleapis.com/bucket/audio" + (i + 1) + ".mp3");
        }

        String jsonContent = objectMapper.writeValueAsString(episodes);
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "episodes.json",
                        MediaType.APPLICATION_JSON_VALUE,
                        jsonContent.getBytes(StandardCharsets.UTF_8));

        // Act
        mockMvc
                .perform(
                        multipart("/api/admin/automation/upload-json")
                                .file(file)
                                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.episodes.length()").value(3));

        // Assert
        assertThat(episodeRepository.count()).isEqualTo(3);

        // Verify each episode has unique data
        var savedEpisodes = episodeRepository.findAll();
        assertThat(savedEpisodes).extracting(Episode::getTitle).contains("Episode 1", "Episode 2", "Episode 3");
    }

    // Helper methods

    private User createUser(String username, String email, String password, Set<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreationDate(OffsetDateTime.now());
        user.setEnabled(true);
        user.setSecondsListened(0L);
        user.setRoles(roles);
        return user;
    }

    private EpisodeJsonDto createValidEpisodeJsonDto() {
        EpisodeJsonDto.ArticleData article = new EpisodeJsonDto.ArticleData();
        article.setTitle("Test Article");
        article.setCategory("Technology");
        article.setAuthor("Test Author");
        article.setPublisher("Test Publisher");
        article.setPublicationDate("2024-12-01T10:00:00Z");
        article.setContentRawUrl("https://storage.googleapis.com/bucket/article.txt");
        article.setScriptUrl("https://storage.googleapis.com/bucket/script.txt");

        EpisodeJsonDto.AudioData audio = new EpisodeJsonDto.AudioData();
        audio.setDuration(180L);
        audio.setFormat("mp3");
        audio.setUrlPath("https://storage.googleapis.com/bucket/audio.mp3");

        EpisodeJsonDto.EpisodeData episode = new EpisodeJsonDto.EpisodeData();
        episode.setTitle("Test Episode");
        episode.setDescription("Test description");
        episode.setScriptUrlPath("https://storage.googleapis.com/bucket/episode-script.txt");
        episode.setImageUrl("https://storage.googleapis.com/bucket/image.jpg");

        EpisodeJsonDto dto = new EpisodeJsonDto();
        dto.setArticle(article);
        dto.setAudio(audio);
        dto.setEpisode(episode);

        return dto;
    }
}
