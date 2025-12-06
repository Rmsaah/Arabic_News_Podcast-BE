package com.shakhbary.arabic_news_podcast.models;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "username", nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "email", nullable = false, unique = true, length = 120)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "first_name", length = 80)
  private String firstName;

  @Column(name = "last_name", length = 80)
  private String lastName;

  @Column(name = "seconds_listened", nullable = false)
  private long secondsListened = 0L;

  @CreatedDate
  @Column(name = "creation_date", nullable = false, updatable = false)
  private OffsetDateTime creationDate;

  @Column(name = "last_login_date")
  private OffsetDateTime lastLoginDate;

  /* RELATIONAL MAPPINGS */

  @ToString.Exclude
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Rating> ratings = new ArrayList<>();

  @ToString.Exclude
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EpisodeProgress> episodeProgress = new ArrayList<>();

  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  /**
   * Accumulates listening time for the user
   *
   * @param additionalSeconds the number of seconds to add to the user's total listening time
   */
  public void addListeningTime(long additionalSeconds) {
    if (additionalSeconds > 0) {
      this.secondsListened += additionalSeconds;
    }
  }
}
