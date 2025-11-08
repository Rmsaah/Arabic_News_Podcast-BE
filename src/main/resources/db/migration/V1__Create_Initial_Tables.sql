-- V1: Create initial database schema for podcast application
-- Creates: users, articles, audios, episodes tables

-- =============================================================================
-- USERS TABLE
-- =============================================================================
CREATE TABLE users (
    id BINARY(16) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(80),
    last_name VARCHAR(80),
    creation_date TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_login_date TIMESTAMP(6) NULL,
    seconds_listened BIGINT NOT NULL DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- ARTICLES TABLE
-- =============================================================================
CREATE TABLE articles (
    id BINARY(16) NOT NULL PRIMARY KEY,
    author VARCHAR(150),
    publisher VARCHAR(150),
    category VARCHAR(100),
    title VARCHAR(150) NOT NULL,
    publication_date TIMESTAMP NULL,
    content_raw_url TEXT NOT NULL,
    script_url TEXT NOT NULL,
    fetch_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_article_category (category),
    INDEX idx_article_author (author),
    INDEX idx_article_fetch_date (fetch_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- AUDIOS TABLE
-- =============================================================================
CREATE TABLE audios (
    id BINARY(16) NOT NULL PRIMARY KEY,
    article_id BINARY(16) NOT NULL,
    duration BIGINT NOT NULL,
    format VARCHAR(20) NOT NULL,
    url_path TEXT NOT NULL,
    creation_date TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    INDEX idx_audio_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- EPISODES TABLE
-- =============================================================================
CREATE TABLE episodes (
    id BINARY(16) NOT NULL PRIMARY KEY,
    article_id BINARY(16) NOT NULL,
    audio_id BINARY(16) NOT NULL UNIQUE,
    title VARCHAR(250) NOT NULL,
    description VARCHAR(1000),
    script_url_path TEXT NOT NULL,
    creation_date TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    image_url VARCHAR(500),
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (audio_id) REFERENCES audios(id) ON DELETE CASCADE,
    INDEX idx_episode_article_id (article_id),
    INDEX idx_episode_audio_id (audio_id),
    INDEX idx_episode_creation_date (creation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
