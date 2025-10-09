package com.shakhbary.arabic_news_podcast.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "articles",
        indexes = {
                @Index(name = "idx_article_category", columnList = "category"),
                @Index(name = "idx_article_author", columnList = "author"),
                @Index(name = "idx_article_fetched_at", columnList = "fetched_at")
        })
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /* DATA */

    @Column(name = "author", length = 150)
    private String author;

    @Column(name = "publisher", nullable = false, length = 150)
    private String publisher;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    @Column(name = "content_raw", nullable = false)
    private String contentRaw;

    @Column(name = "content_cleaned", nullable = false)
    private String contentCleaned;

    @Column(name = "url_path", nullable = false)
    private String urlPath;

    @CreatedDate
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private OffsetDateTime fetchedAt;

    /* RELATIONAL MAPPINGS */

    @ToString.Exclude
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Audio> audioFiles = new ArrayList<>(); // Article HAS MANY Audio

    @ToString.Exclude
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Episode> episodes = new ArrayList<>(); // Article HAS MANY Episodes
}
