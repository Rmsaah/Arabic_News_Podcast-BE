package com.shakhbary.arabic_news_podcast.services;

import com.google.cloud.storage.*;
import com.shakhbary.arabic_news_podcast.services.CloudStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

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

    @Override
    public String uploadFile(String localFilePath, String cloudFileName, String mimeType) {
        try {
            // Read file content
            byte[] fileContent = Files.readAllBytes(Paths.get(localFilePath));

            // Create blob ID and info
            BlobId blobId = BlobId.of(bucketName, cloudFileName);
            BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(blobId)
                    .setContentType(mimeType);

            // Make file publicly readable if configured
            if (makePublic) {
                blobInfoBuilder.setAcl(java.util.List.of(
                        Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)
                ));
            }

            BlobInfo blobInfo = blobInfoBuilder.build();

            // Upload to Google Cloud Storage
            Blob blob = storage.create(blobInfo, fileContent);

            // Generate public URL
            String publicUrl = String.format("https://storage.googleapis.com/%s/%s",
                    bucketName, cloudFileName);

            log.info("Uploaded file to GCS: {} -> {}", localFilePath, publicUrl);

            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to upload file to GCS: {}", localFilePath, e);
            throw new RuntimeException("Failed to upload file to Google Cloud Storage", e);
        }
    }

    @Override
    public String uploadTextContent(String content, String cloudFileName) {
        try {
            // Create blob ID and info
            BlobId blobId = BlobId.of(bucketName, cloudFileName);
            BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(blobId)
                    .setContentType("text/plain; charset=utf-8");

            // Make file publicly readable if configured
            if (makePublic) {
                blobInfoBuilder.setAcl(java.util.List.of(
                        Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)
                ));
            }

            BlobInfo blobInfo = blobInfoBuilder.build();

            // Upload text content
            Blob blob = storage.create(blobInfo, content.getBytes());

            // Generate public URL
            String publicUrl = String.format("https://storage.googleapis.com/%s/%s",
                    bucketName, cloudFileName);

            log.info("Uploaded text content to GCS: {}", publicUrl);

            return publicUrl;

        } catch (Exception e) {
            log.error("Failed to upload text content to GCS: {}", cloudFileName, e);
            throw new RuntimeException("Failed to upload text content to Google Cloud Storage", e);
        }
    }

    @Override
    public boolean deleteFile(String cloudFileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, cloudFileName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("Deleted file from GCS: {}", cloudFileName);
            } else {
                log.warn("File not found in GCS for deletion: {}", cloudFileName);
            }

            return deleted;

        } catch (Exception e) {
            log.error("Failed to delete file from GCS: {}", cloudFileName, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String cloudFileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, cloudFileName);
            Blob blob = storage.get(blobId);
            boolean exists = blob != null && blob.exists();

            log.debug("File exists check in GCS: {} - {}", cloudFileName, exists);

            return exists;

        } catch (Exception e) {
            log.error("Failed to check file existence in GCS: {}", cloudFileName, e);
            return false;
        }
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
