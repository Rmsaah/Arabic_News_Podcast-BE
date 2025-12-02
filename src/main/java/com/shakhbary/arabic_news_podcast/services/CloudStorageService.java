package com.shakhbary.arabic_news_podcast.services;

/**
 * Service for cloud storage operations (Google Cloud Storage).
 *
 * Note: This application follows a clean architecture approach where admins upload
 * files to GCS externally, and the app only stores/uses the resulting URLs.
 * The upload methods (uploadFile, uploadTextContent) are kept for potential future use
 * but are not currently utilized by the application.
 */
public interface CloudStorageService {

    /**
     * Generate a signed URL for temporary private access to a file.
     * Useful for audio streaming without making files permanently public.
     *
     * ACTIVELY USED by AudioStreamingServiceImpl for secure audio streaming.
     *
     * @param cloudFileName The file name in cloud storage
     * @param durationMinutes How long the URL should be valid (in minutes)
     * @return Signed URL that expires after the specified duration
     */
    String generateSignedUrl(String cloudFileName, long durationMinutes);
}