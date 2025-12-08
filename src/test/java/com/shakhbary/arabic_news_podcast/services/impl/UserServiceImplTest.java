package com.shakhbary.arabic_news_podcast.services.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shakhbary.arabic_news_podcast.dtos.UserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserRegistrationRequestDto;
import com.shakhbary.arabic_news_podcast.exceptions.DuplicateResourceException;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.models.Role;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.RoleRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;
import com.shakhbary.arabic_news_podcast.services.Impl.UserServiceImpl;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for UserServiceImpl.
 *
 * Tests authentication service logic including:
 * - User registration with password hashing (BCrypt)
 * - Duplicate username/email detection
 * - Role assignment (ROLE_USER by default)
 * - User access validation and authorization
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")  // Add this if using application-test.properties
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;

    @Mock private RoleRepository roleRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    private UserRegistrationRequestDto validRegistrationRequest;
    private Role userRole;
    private User savedUser;

    @BeforeEach
    void setUp() {
        // Prepare valid registration request
        validRegistrationRequest = new UserRegistrationRequestDto();
        validRegistrationRequest.setUsername("testuser");
        validRegistrationRequest.setEmail("test@example.com");
        validRegistrationRequest.setPassword("password123");
        validRegistrationRequest.setFirstName("Test");
        validRegistrationRequest.setLastName("User");

        // Prepare role
        userRole = new Role();
        userRole.setId(UUID.randomUUID());
        userRole.setName("ROLE_USER");

        // Prepare saved user
        savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("$2a$10$hashedPassword");
        savedUser.setFirstName("Test");
        savedUser.setLastName("User");
        savedUser.setCreationDate(OffsetDateTime.now());
        savedUser.setEnabled(true);
        savedUser.setSecondsListened(0L);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        savedUser.setRoles(roles);
    }

    @Test
    @DisplayName("Should successfully register new user with hashed password")
    void testRegisterNewUser_Success() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserDto result = userService.registerNewUser(validRegistrationRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");

        // Verify password was encoded
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPassword()).isEqualTo("$2a$10$hashedPassword");
        assertThat(capturedUser.isEnabled()).isTrue();
        assertThat(capturedUser.getSecondsListened()).isEqualTo(0L);

        // Verify role assignment
        assertThat(capturedUser.getRoles()).hasSize(1);
        assertThat(capturedUser.getRoles()).contains(userRole);

        // Verify interactions
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("ROLE_USER");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when username already exists")
    void testRegisterNewUser_DuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerNewUser(validRegistrationRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username is already taken");

        // Verify no save occurred
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void testRegisterNewUser_DuplicateEmail() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerNewUser(validRegistrationRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email is already registered");

        // Verify no save occurred
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should throw RuntimeException when ROLE_USER not found in database")
    void testRegisterNewUser_RoleNotFound() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.registerNewUser(validRegistrationRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Default role ROLE_USER not found");

        // Verify no save occurred
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully get user when requesting own profile")
    void testGetUser_OwnProfile() {
        // Arrange
        UUID userId = savedUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));

        // Act
        UserDto result = userService.getUser(userId, "testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testGetUser_UserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUser(userId, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw ResponseStatusException when non-admin tries to access other user")
    void testGetUser_AccessDenied() {
        // Arrange
        UUID userId = savedUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));

        // Mock security context with non-admin user
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenAnswer(invocation -> java.util.Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThatThrownBy(() -> userService.getUser(userId, "differentuser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You can only view this profile");

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should allow admin to access any user profile")
    void testGetUser_AdminAccess() {
        // Arrange
        UUID userId = savedUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));

        // Mock security context with admin user
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenAnswer(invocation -> java.util.Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.setContext(securityContext);

        // Act
        UserDto result = userService.getUser(userId, "adminuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should successfully update user name")
    void testUpdateUserName_Success() {
        // Arrange
        UUID userId = savedUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserDto result = userService.updateUserName(userId, "NewFirst", "NewLast", "testuser");

        // Assert
        assertThat(result).isNotNull();

        // Verify user fields were updated
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getFirstName()).isEqualTo("NewFirst");
        assertThat(capturedUser.getLastName()).isEqualTo("NewLast");
    }

    @Test
    @DisplayName("Should register user with minimal information (no first/last name)")
    void testRegisterNewUser_MinimalInfo() {
        // Arrange
        UserRegistrationRequestDto minimalRequest = new UserRegistrationRequestDto();
        minimalRequest.setUsername("minimal");
        minimalRequest.setEmail("minimal@example.com");
        minimalRequest.setPassword("password123");
        // No first/last name

        User minimalUser = new User();
        minimalUser.setId(UUID.randomUUID());
        minimalUser.setUsername("minimal");
        minimalUser.setEmail("minimal@example.com");
        minimalUser.setPassword("$2a$10$hashedPassword");
        minimalUser.setCreationDate(OffsetDateTime.now());
        minimalUser.setEnabled(true);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        minimalUser.setRoles(roles);

        when(userRepository.existsByUsername("minimal")).thenReturn(false);
        when(userRepository.existsByEmail("minimal@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(minimalUser);

        // Act
        UserDto result = userService.registerNewUser(minimalRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("minimal");
        assertThat(result.getEmail()).isEqualTo("minimal@example.com");
    }
}
