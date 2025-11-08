package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.services.AudioStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for audio streaming endpoints.
 * Provides URLs for playing podcast episode audio files with support for both
 * public URLs and signed URLs with temporary access.
 * Only enabled when spring.cloud.gcp.storage.enabled=true
 */
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cloud.gcp.storage.enabled", havingValue = "true")
public class AudioController {

    private final AudioStreamingService audioStreamingService;

    /**
     * Get a playable URL for an audio file.
     * <p>
     * Returns either:
     * <ul>
     *   <li>Direct public URL (when app.audio.use-signed-urls=false) - Best performance, CDN-like delivery</li>
     *   <li>Signed URL with expiration (when app.audio.use-signed-urls=true) - For private/paid content</li>
     * </ul>
     *
     * @param audioId The UUID of the audio file
     * @return Response containing the stream URL and expiration time in seconds
     */
    @GetMapping("/{audioId}/stream-url")
    public ResponseEntity<AudioStreamUrlResponse> getStreamUrl(@PathVariable UUID audioId) {
        String streamUrl = audioStreamingService.getStreamUrl(audioId);
        long expiresIn = audioStreamingService.getUrlExpirationTime();

        return ResponseEntity.ok(new AudioStreamUrlResponse(streamUrl, expiresIn));
    }

    /**
     * Response DTO for stream URL endpoint.
     *
     * @param streamUrl Direct URL to the audio file (Google Cloud Storage URL)
     * @param expiresInSeconds How long the URL is valid in seconds (relevant for signed URLs, for public URLs this is just informational)
     */
    public record AudioStreamUrlResponse(
            String streamUrl,
            long expiresInSeconds
    ) {}
}