package com.shakhbary.arabic_news_podcast.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shakhbary.arabic_news_podcast.dtos.LoginRequestDto;
import com.shakhbary.arabic_news_podcast.dtos.UserRegistrationRequestDto;
import com.shakhbary.arabic_news_podcast.models.Role;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.RoleRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for AuthController.
 *
 * Tests authentication endpoints with MockMvc:
 * - User registration flow
 * - Login flow with password verification
 * - Basic Auth credential generation
 * - Validation and error handling
 */
@SpringBootTest
@ActiveProfiles("test")  // Add this if using application-test.properties
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:src/test/resources/application-test.properties")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    private Role userRole;

    @BeforeEach
    void setUp() {
        // Clean database
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create ROLE_USER
        userRole = new Role();
        userRole.setName("ROLE_USER");
        roleRepository.save(userRole);
    }

    @Test
    @DisplayName("Should successfully register new user")
    void testRegisterUser_Success() throws Exception {
        // Arrange
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        // Act & Assert
        MvcResult result =
                mockMvc
                        .perform(
                                post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.username").value("newuser"))
                        .andExpect(jsonPath("$.email").value("newuser@example.com"))
                        .andExpect(jsonPath("$.firstName").value("New"))
                        .andExpect(jsonPath("$.lastName").value("User"))
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.creationDate").exists())
                        .andReturn();

        // Verify user was saved to database
        User savedUser = userRepository.findByUsername("newuser").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.isEnabled()).isTrue();

        // Verify password was hashed
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();

        // Verify role assignment
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Should reject registration with duplicate username")
    void testRegisterUser_DuplicateUsername() throws Exception {
        // Arrange - Create existing user
        User existingUser = createUser("existinguser", "existing@example.com", "password123");
        userRepository.save(existingUser);

        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUsername("existinguser");
        request.setEmail("different@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testRegisterUser_DuplicateEmail() throws Exception {
        // Arrange - Create existing user
        User existingUser = createUser("existinguser", "existing@example.com", "password123");
        userRepository.save(existingUser);

        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUsername("differentuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    @DisplayName("Should reject registration with invalid email format")
    void testRegisterUser_InvalidEmail() throws Exception {
        // Arrange
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUsername("newuser");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with short username")
    void testRegisterUser_ShortUsername() throws Exception {
        // Arrange
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUsername("ab"); // Less than 3 characters
        request.setEmail("user@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with short password")
    void testRegisterUser_ShortPassword() throws Exception {
        // Arrange
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUsername("newuser");
        request.setEmail("user@example.com");
        request.setPassword("12345"); // Less than 6 characters

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with missing required fields")
    void testRegisterUser_MissingFields() throws Exception {
        // Arrange - Empty request
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() throws Exception {
        // Arrange - Create user
        User user = createUser("testuser", "test@example.com", "password123");
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.credentials").exists())
                .andExpect(jsonPath("$.authType").value("Basic"));

        // Verify last login date was updated
        User updatedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(updatedUser.getLastLoginDate()).isNotNull();
    }

    @Test
    @DisplayName("Should reject login with invalid username")
    void testLogin_InvalidUsername() throws Exception {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void testLogin_InvalidPassword() throws Exception {
        // Arrange - Create user
        User user = createUser("testuser", "test@example.com", "correctpassword");
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("Should reject login for disabled account")
    void testLogin_DisabledAccount() throws Exception {
        // Arrange - Create disabled user
        User user = createUser("disableduser", "disabled@example.com", "password123");
        user.setEnabled(false);
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("disableduser");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account is disabled"));
    }

    @Test
    @DisplayName("Should return properly encoded Basic Auth credentials")
    void testLogin_CredentialsEncoding() throws Exception {
        // Arrange - Create user
        User user = createUser("testuser", "test@example.com", "password123");
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Act
        MvcResult result =
                mockMvc
                        .perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn();

        // Assert
        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("credentials");
        assertThat(response).contains("Basic");

        // Verify credentials format (Base64 encoded)
        String credentials = objectMapper.readTree(response).get("credentials").asText();
        assertThat(credentials).matches("^[A-Za-z0-9+/=]+$"); // Base64 pattern
    }

    @Test
    @DisplayName("Should register user with minimal information")
    void testRegisterUser_MinimalInfo() throws Exception {
        // Arrange - Only required fields
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUsername("minimaluser");
        request.setEmail("minimal@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc
                .perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("minimaluser"))
                .andExpect(jsonPath("$.email").value("minimal@example.com"));
    }

    // Helper method to create test users
    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreationDate(OffsetDateTime.now());
        user.setEnabled(true);
        user.setSecondsListened(0L);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return user;
    }
}
