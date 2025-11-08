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
     * Upload a file to cloud storage and return public URL.
     *
     * NOTE: This method is NOT currently used by the application.
     * Files should be uploaded to GCS externally by admins.
     * Kept for potential future administrative features.
     *
     * @param localFilePath Path to local file
     * @param cloudFileName Desired filename in cloud storage
     * @param mimeType MIME type of the file
     * @return Public URL of uploaded file
     */
    String uploadFile(String localFilePath, String cloudFileName, String mimeType);

    /**
     * Upload content as text file to cloud storage.
     *
     * NOTE: This method is NOT currently used by the application.
     * Files should be uploaded to GCS externally by admins.
     * Kept for potential future administrative features.
     *
     * @param content Text content to upload
     * @param cloudFileName Desired filename in cloud storage
     * @return Public URL of uploaded file
     */
    String uploadTextContent(String content, String cloudFileName);

    /**
     * Delete a file from cloud storage.
     *
     * NOTE: This method is NOT currently used by the application.
     * Kept for potential future administrative features.
     *
     * @param cloudFileName Filename in cloud storage
     * @return true if deleted successfully
     */
    boolean deleteFile(String cloudFileName);

    /**
     * Check if a file exists in cloud storage.
     *
     * NOTE: This method is NOT currently used by the application.
     * Kept for potential future administrative features.
     *
     * @param cloudFileName Filename to check
     * @return true if file exists
     */
    boolean fileExists(String cloudFileName);

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