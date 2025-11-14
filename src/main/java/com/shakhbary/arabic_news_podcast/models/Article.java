package com.shakhbary.arabic_news_podcast.models;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "articles",
        indexes = {
                @Index(name = "idx_article_category", columnList = "category"),
                @Index(name = "idx_article_author", columnList = "author"),
                @Index(name = "idx_article_fetched_at", columnList = "fetched_at"),
                @Index(name = "idx_article_status", columnList = "status")  // NEW
        })
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /* DATA */

    @Column(name = "author", length = 150)
    private String author;

    @Column(name = "publisher", length = 150)
    private String publisher;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "publication_date")
    private OffsetDateTime publicationDate;

    @Column(name = "content_raw", nullable = false, columnDefinition = "LONGTEXT")
    private String contentRaw;

    @Column(name = "content_cleaned", nullable = false, columnDefinition = "LONGTEXT")
    private String contentCleaned;

    @Column(name = "url_path", nullable = false)
    private String urlPath;

    @CreatedDate
    @Column(name = "fetch_date", nullable = false, updatable = false)
    private OffsetDateTime fetchDate;

    // NEW: Track processing status
    @Column(name = "status", length = 20)
    private String status; // SCRAPED, SCRIPT_GENERATED, AUDIO_GENERATED, COMPLETED, FAILED

    /* RELATIONAL MAPPINGS */

    @ToString.Exclude
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Audio> audioFiles = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Episode> episodes = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        if (fetchedAt == null) {
            fetchedAt = OffsetDateTime.now();
        }
        if (status == null) {
            status = "SCRAPED";
        }
    }
}