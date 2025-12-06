package com.shakhbary.arabic_news_podcast.repositories;

import com.shakhbary.arabic_news_podcast.models.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
