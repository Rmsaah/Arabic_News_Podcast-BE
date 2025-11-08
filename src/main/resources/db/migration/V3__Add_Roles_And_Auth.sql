-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
    );

-- Create user_roles join table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

-- Add enabled column to users table (if it doesn't already exist)
-- MySQL doesn't support IF NOT EXISTS in ALTER TABLE, so we use a stored procedure workaround
SET @dbname = DATABASE();
SET @tablename = 'users';
SET @columnname = 'enabled';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',  -- Column exists, do nothing
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BOOLEAN NOT NULL DEFAULT TRUE')
));
PREPARE addColumnIfNotExists FROM @preparedStatement;
EXECUTE addColumnIfNotExists;
DEALLOCATE PREPARE addColumnIfNotExists;

-- Insert default roles (ignore if they already exist)
INSERT IGNORE INTO roles (id, name) VALUES (UNHEX(REPLACE(UUID(), '-', '')), 'ROLE_USER');
INSERT IGNORE INTO roles (id, name) VALUES (UNHEX(REPLACE(UUID(), '-', '')), 'ROLE_ADMIN');
