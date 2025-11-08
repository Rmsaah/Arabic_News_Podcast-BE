-- V2: Create user interaction tables
-- Creates: ratings, episode_progress tables

-- =============================================================================
-- RATINGS TABLE
-- =============================================================================
CREATE TABLE ratings (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    episode_id BINARY(16) NOT NULL,
    rating INT NOT NULL,
    rating_date TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_episode_rating (user_id, episode_id),
    INDEX idx_ratings_user_id (user_id),
    INDEX idx_ratings_episode_id (episode_id),
    CHECK (rating >= 1 AND rating <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- EPISODE_PROGRESS TABLE
-- =============================================================================
CREATE TABLE episode_progress (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    episode_id BINARY(16) NOT NULL,
    last_position_seconds BIGINT NOT NULL DEFAULT 0,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    last_played_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    play_count INT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_episode_progress (user_id, episode_id),
    INDEX idx_progress_user_id (user_id),
    INDEX idx_progress_episode_id (episode_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
