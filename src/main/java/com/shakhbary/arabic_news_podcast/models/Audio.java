package com.shakhbary.arabic_news_podcast.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audios",
        indexes = {
                @Index(name = "idx_audio_article_id", columnList = "article_id")
        })
public class Audio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /* FOREIGN KEYS */

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false, unique = true)
    private Article article;

    /* DATA */

    @Column(name = "duration", nullable = false)
    private long duration; // in seconds

    @Column(name = "format", nullable = false, length = 20)
    private String format;

    @Column(name = "url_path", nullable = false)
    private String urlPath;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /* RELATIONAL MAPPINGS */

    @ToString.Exclude
    @OneToOne(mappedBy = "audio")
    private Episode episode;

}
