package com.shakhbary.arabic_news_podcast.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "episodes")
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY) //fetch = FetchType.LAZY is recommended for performance; the related entity (e.g., Article) is only loaded from the database when you explicitly call a getter for it.
    @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false, unique = true)
    private Article articleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_id", referencedColumnName = "id", nullable = false, unique = true)
    private Audio audio_id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "transcript", nullable = false)
    private String transcript;

    @Column(name = "image_url")
    private String imageUrl;

    @CreatedDate // Tells Spring to auto-populate this on creation
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

}


