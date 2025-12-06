package com.shakhbary.arabic_news_podcast.models;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "ratings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "episode_id"}),
    indexes = {
      @Index(name = "idx_ratings_episode_id", columnList = "episode_id"),
      @Index(name = "idx_ratings_user_id", columnList = "user_id")
    })
public class Rating {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /* FOREIGN KEYS */

  @ToString.Exclude
  @ManyToOne(
      fetch =
          FetchType
              .LAZY) // fetch = FetchType.LAZY is recommended for performance; the related entity
  // (e.g., Article) is only loaded from the database when you explicitly call a
  // getter for it.
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @ToString.Exclude
  @ManyToOne(
      fetch =
          FetchType
              .LAZY) // fetch = FetchType.LAZY is recommended for performance; the related entity
  // (e.g., Article) is only loaded from the database when you explicitly call a
  // getter for it.
  @JoinColumn(name = "episode_id", referencedColumnName = "id", nullable = false)
  private Episode episode;

  /* DATA */

  @Column(name = "rating", nullable = false)
  private int rating; // from 1 to 5

  @CreatedDate
  @Column(name = "rating_date", nullable = false)
  private OffsetDateTime ratingDate;
}
