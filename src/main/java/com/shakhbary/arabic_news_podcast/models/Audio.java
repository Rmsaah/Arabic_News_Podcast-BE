package com.shakhbary.arabic_news_podcast.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "audios")
public class Audio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY) //fetch = FetchType.LAZY is recommended for performance; the related entity (e.g., Article) is only loaded from the database when you explicitly call a getter for it.
    @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false, unique = true)
    private Article articleId;

    @Column(name = "duration", nullable = false)
    private long duration; // in seconds

    @Column(name = "format", nullable = false, length = 20)
    private String format;

    @Column(name = "url_path", nullable = false)
    private String urlPath;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

}
