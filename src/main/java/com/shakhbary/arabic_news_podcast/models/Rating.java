package com.shakhbary.arabic_news_podcast.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) //fetch = FetchType.LAZY is recommended for performance; the related entity (e.g., Article) is only loaded from the database when you explicitly call a getter for it.
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User userId;

    @ManyToOne(fetch = FetchType.LAZY) //fetch = FetchType.LAZY is recommended for performance; the related entity (e.g., Article) is only loaded from the database when you explicitly call a getter for it.
    @JoinColumn(name = "episode_id", referencedColumnName = "id", nullable = false, unique = true)
    private Episode episodeId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @CreatedDate
    @Column(name = "rated_at", nullable = false)
    private OffsetDateTime ratedAt;

}
