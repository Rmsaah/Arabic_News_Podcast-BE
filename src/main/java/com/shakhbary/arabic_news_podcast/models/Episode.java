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
@Table(name = "episodes")
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false, unique = true)
    private Article article;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_id", referencedColumnName = "id", nullable = false, unique = true)
    private Audio audio;

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

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EpisodeCompletion> completions = new ArrayList<>();

}
