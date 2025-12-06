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
    name = "audios",
    indexes = {@Index(name = "idx_audio_article_id", columnList = "article_id")})
public class Audio {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /* FOREIGN KEYS */

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)
  private Article article;

  /* DATA */

  @Column(name = "duration", nullable = false)
  private long duration; // in seconds

  @Column(name = "format", nullable = false, length = 20)
  private String format;

  @Column(name = "url_path", nullable = false)
  private String urlPath;

  @CreatedDate
  @Column(name = "creation_date", nullable = false, updatable = false)
  private OffsetDateTime creationDate;

  /* RELATIONAL MAPPINGS */

  @ToString.Exclude
  @OneToOne(mappedBy = "audio")
  private Episode episode;
}
