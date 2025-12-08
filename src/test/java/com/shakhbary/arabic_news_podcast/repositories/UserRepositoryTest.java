package com.shakhbary.arabic_news_podcast.repositories;

import static org.assertj.core.api.Assertions.*;

import com.shakhbary.arabic_news_podcast.models.Role;
import com.shakhbary.arabic_news_podcast.models.User;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for UserRepository.
 *
 * Uses @DataJpaTest with H2 in-memory database for isolated testing.
 * Tests custom repository methods and database queries.
 */
@DataJpaTest
@ActiveProfiles("test")  // Add this if using application-test.properties
@TestPropertySource(locations = "file:src/test/resources/application-test.properties")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired private TestEntityManager entityManager;

    @Autowired private UserRepository userRepository;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Create and persist role
        userRole = new Role();
        userRole.setName("ROLE_USER");
        entityManager.persist(userRole);

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreationDate(OffsetDateTime.now());
        testUser.setEnabled(true);
        testUser.setSecondsListened(0L);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername_Success() {
        // Act
        Optional<User> found = userRepository.findByUsername("testuser");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void testFindByUsername_NotFound() {
        // Act
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail_Success() {
        // Act
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void testFindByEmail_NotFound() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void testExistsByEmail_True() {
        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void testExistsByEmail_False() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true when username exists")
    void testExistsByUsername_True() {
        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void testExistsByUsername_False() {
        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should save new user with all fields")
    void testSaveUser_Success() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("$2a$10$anotherHashedPassword");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setCreationDate(OffsetDateTime.now());
        newUser.setEnabled(true);
        newUser.setSecondsListened(0L);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);

        // Act
        User saved = userRepository.save(newUser);
        entityManager.flush();

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("newuser");
        assertThat(saved.getEmail()).isEqualTo("new@example.com");

        // Verify it's retrievable
        Optional<User> found = userRepository.findByUsername("newuser");
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("Should update user listening time")
    void testUpdateUser_SecondsListened() {
        // Arrange
        User user = userRepository.findByUsername("testuser").orElseThrow();
        user.addListeningTime(300L); // Add 5 minutes

        // Act
        userRepository.save(user);
        entityManager.flush();

        // Assert
        User updated = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(updated.getSecondsListened()).isEqualTo(300L);
    }

    @Test
    @DisplayName("Should load user with eager-loaded roles")
    void testFindUser_WithRoles() {
        // Act
        User found = userRepository.findByUsername("testuser").orElseThrow();

        // Assert - roles should be eagerly loaded
        assertThat(found.getRoles()).isNotEmpty();
        assertThat(found.getRoles()).hasSize(1);
        assertThat(found.getRoles().iterator().next().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Should handle case-sensitive username search")
    void testFindByUsername_CaseSensitive() {
        // Act
        Optional<User> lowercase = userRepository.findByUsername("testuser");
        Optional<User> uppercase = userRepository.findByUsername("TESTUSER");

        // Assert
        assertThat(lowercase).isPresent();
        assertThat(uppercase).isEmpty(); // Should be case-sensitive
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Arrange - add another user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("$2a$10$hashedPassword");
        anotherUser.setCreationDate(OffsetDateTime.now());
        anotherUser.setEnabled(true);
        anotherUser.setSecondsListened(0L);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        anotherUser.setRoles(roles);
        entityManager.persistAndFlush(anotherUser);

        // Act
        var allUsers = userRepository.findAll();

        // Assert
        assertThat(allUsers).hasSize(2);
    }

    @Test
    @DisplayName("Should delete user by id")
    void testDeleteUser() {
        // Arrange
        User user = userRepository.findByUsername("testuser").orElseThrow();

        // Act
        userRepository.deleteById(user.getId());
        entityManager.flush();

        // Assert
        Optional<User> deleted = userRepository.findByUsername("testuser");
        assertThat(deleted).isEmpty();
    }
}
