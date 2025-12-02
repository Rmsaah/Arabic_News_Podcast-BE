package com.shakhbary.arabic_news_podcast.services.Impl;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.shakhbary.arabic_news_podcast.services.CloudStorageService;

import lombok.extern.slf4j.Slf4j;

/**
 * Google Cloud Storage implementation for production file storage.
 * Handles uploading, deleting, and checking files in Google Cloud Storage.
 *
 * Uses Spring Cloud GCP autoconfiguration for Storage client.
 * Only enabled when spring.cloud.gcp.storage.enabled=true
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.cloud.gcp.storage.enabled", havingValue = "true")
public class CloudStorageServiceImpl implements CloudStorageService {

    private final Storage storage;
    private final String bucketName;
    private final boolean makePublic;

    /**
     * Constructor with Spring Cloud GCP autoconfigured Storage bean.
     * The Storage client is automatically configured by spring-cloud-gcp-starter-storage.
     *
     * Authentication: Uses Application Default Credentials (ADC)
     * - In production: set GOOGLE_APPLICATION_CREDENTIALS environment variable
     * - In development: use `gcloud auth application-default login`
     */
    public CloudStorageServiceImpl(
            Storage storage,
            @Value("${gcp.storage.bucket-name}") String bucketName,
            @Value("${gcp.storage.make-public:true}") boolean makePublic) {
        this.storage = storage;
        this.bucketName = bucketName;
        this.makePublic = makePublic;

        log.info("Initialized Google Cloud Storage with bucket: {}", bucketName);
    }

    /**
     * Generate a signed URL for temporary private access to a file.
     * Useful for audio streaming without making files permanently public.
     *
     * @param cloudFileName The file name in cloud storage
     * @param durationMinutes How long the URL should be valid (in minutes)
     * @return Signed URL that expires after the specified duration
     */
    public String generateSignedUrl(String cloudFileName, long durationMinutes) {
        try {
            BlobId blobId = BlobId.of(bucketName, cloudFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            // Generate signed URL valid for specified duration
            java.net.URL signedUrl = storage.signUrl(
                    blobInfo,
                    durationMinutes,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.withV4Signature()
            );

            log.debug("Generated signed URL for: {} (valid for {} minutes)",
                    cloudFileName, durationMinutes);

            return signedUrl.toString();

        } catch (Exception e) {
            log.error("Failed to generate signed URL for: {}", cloudFileName, e);
            throw new RuntimeException("Failed to generate signed URL", e);
        }
    }
}