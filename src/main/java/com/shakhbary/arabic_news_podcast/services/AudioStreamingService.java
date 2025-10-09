package com.shakhbary.arabic_news_podcast.services;

import java.util.UUID;

/**
 * Service for generating playable audio URLs for podcast episodes.
 * Supports both public URLs (direct GCS access) and signed URLs (temporary private access).
 */
public interface AudioStreamingService {

    /**
     * Get a playable URL for the audio file.
     * Returns signed URL for private files or direct URL for public files.
     *
     * @param audioId The UUID of the audio file
     * @return Playable URL (direct GCS URL or signed URL depending on configuration)
     */
    String getStreamUrl(UUID audioId);

    /**
     * Get URL expiration time in seconds.
     * Only relevant when using signed URLs (app.audio.use-signed-urls=true).
     *
     * @return Expiration time in seconds
     */
    long getUrlExpirationTime();
}
