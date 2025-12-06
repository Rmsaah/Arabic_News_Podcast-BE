package com.shakhbary.arabic_news_podcast.repositories;

import com.shakhbary.arabic_news_podcast.models.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Role entity. Provides database access for user roles. */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

  /**
   * Find a role by its name (e.g., "ROLE_USER", "ROLE_ADMIN")
   *
   * @param name The role name
   * @return Optional containing the role if found
   */
  Optional<Role> findByName(String name);
}
