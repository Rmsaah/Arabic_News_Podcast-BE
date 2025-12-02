package com.shakhbary.arabic_news_podcast.services.Imp;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.models.Audio;
import com.shakhbary.arabic_news_podcast.repositories.AudioRepository;
import com.shakhbary.arabic_news_podcast.services.AudioStreamingService;
import com.shakhbary.arabic_news_podcast.services.CloudStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for generating playable URLs for podcast audio files.
 * Returns direct Google Cloud Storage URLs for public files,
 * or signed URLs with temporary access for private files.
 * Only enabled when spring.cloud.gcp.storage.enabled=true
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.cloud.gcp.storage.enabled", havingValue = "true")
public class AudioStreamingServiceImpl implements AudioStreamingService {

    private final AudioRepository audioRepository;
    private final CloudStorageService cloudStorageService;

    @Value("${app.audio.url-expiration-hours:24}")
    private int urlExpirationHours;

    @Value("${app.audio.use-signed-urls:false}")
    private boolean useSignedUrls;

    @Override
    public String getStreamUrl(UUID audioId) {
        Audio audio = audioRepository.findById(audioId)
                .orElseThrow(() -> new ResourceNotFoundException("Audio not found: " + audioId));

        String urlPath = audio.getUrlPath();

        // Generate signed URL for temporary private access if enabled
        if (useSignedUrls && urlPath.contains("googleapis.com")) {
            String fileName = extractFileNameFromGcsUrl(urlPath);
            long durationMinutes = urlExpirationHours * 60L;

            log.debug("Generating signed URL for audio: {} (valid for {} hours)", audioId, urlExpirationHours);
            return cloudStorageService.generateSignedUrl(fileName, durationMinutes);
        } else {
            // Return direct public URL for best performance
            log.debug("Returning direct public URL for audio: {}", audioId);
            return urlPath;
        }
    }

    @Override
    public long getUrlExpirationTime() {
        return Duration.ofHours(urlExpirationHours).toSeconds();
    }

    /**
     * Extract the file path from a Google Cloud Storage URL.
     * Example: https://storage.googleapis.com/bucket-name/path/to/file.mp3 -> path/to/file.mp3
     */
    private String extractFileNameFromGcsUrl(String gcsUrl) {
        try {
            // Format: https://storage.googleapis.com/{bucket}/{path}
            String[] parts = gcsUrl.split("googleapis\\.com/[^/]+/");
            if (parts.length > 1) {
                return parts[1];
            }
            // Fallback: return everything after the third slash
            String[] urlParts = gcsUrl.split("/", 4);
            return urlParts.length > 3 ? urlParts[3] : gcsUrl;
        } catch (Exception e) {
            log.warn("Could not extract filename from GCS URL: {}, using full URL", gcsUrl);
            return gcsUrl;
        }
    }
}