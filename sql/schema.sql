CREATE DATABASE IF NOT EXISTS lab5_social DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE lab5_social;

DROP VIEW IF EXISTS admin_post_review_view;
DROP TABLE IF EXISTS post_audit_logs;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS friend_requests;
DROP TABLE IF EXISTS friendships;
DROP TABLE IF EXISTS friend_groups;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(40) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(40) NOT NULL,
  gender VARCHAR(10),
  birthday DATE,
  age INT,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_users_age CHECK (age IS NULL OR age BETWEEN 0 AND 150)
);

CREATE TABLE admins (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(40) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(40) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE friend_groups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(40) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_friend_groups_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uk_friend_groups_user_name UNIQUE (user_id, name)
);

CREATE TABLE friendships (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  group_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_friendships_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_friendships_group FOREIGN KEY (group_id) REFERENCES friend_groups(id) ON DELETE SET NULL,
  CONSTRAINT uk_friendships_pair UNIQUE (owner_id, friend_id),
  CONSTRAINT chk_friendships_not_self CHECK (owner_id <> friend_id)
);

CREATE TABLE friend_requests (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  requester_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  requester_group_id BIGINT,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  responded_at DATETIME,
  CONSTRAINT fk_friend_requests_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_friend_requests_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_friend_requests_group FOREIGN KEY (requester_group_id) REFERENCES friend_groups(id) ON DELETE SET NULL,
  CONSTRAINT uk_friend_requests_pair_status UNIQUE (requester_id, receiver_id, status),
  CONSTRAINT chk_friend_requests_not_self CHECK (requester_id <> receiver_id),
  CONSTRAINT chk_friend_requests_status CHECK (status IN ('PENDING', 'ACCEPTED'))
);

CREATE TABLE posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  author_id BIGINT NOT NULL,
  content VARCHAR(280) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_posts_content_length CHECK (CHAR_LENGTH(content) BETWEEN 1 AND 280)
);

CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  content VARCHAR(160) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_comments_content_length CHECK (CHAR_LENGTH(content) BETWEEN 1 AND 160)
);

CREATE TABLE post_audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT,
  admin_id BIGINT,
  action VARCHAR(40) NOT NULL,
  reason VARCHAR(200),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_posts_author_updated ON posts(author_id, last_updated_at DESC);
CREATE INDEX idx_posts_content ON posts(content);
CREATE INDEX idx_comments_post_created ON comments(post_id, created_at);
CREATE INDEX idx_comments_content ON comments(content);
CREATE INDEX idx_friendships_owner ON friendships(owner_id);
CREATE INDEX idx_friend_requests_receiver_status ON friend_requests(receiver_id, status, created_at);

CREATE VIEW admin_post_review_view AS
SELECT
  p.id AS post_id,
  p.author_id,
  p.content,
  p.status,
  p.created_at,
  p.last_updated_at,
  COUNT(c.id) AS comment_count
FROM posts p
LEFT JOIN comments c ON c.post_id = p.id
GROUP BY p.id, p.author_id, p.content, p.status, p.created_at, p.last_updated_at;

DELIMITER //

CREATE TRIGGER trg_posts_before_update
BEFORE UPDATE ON posts
FOR EACH ROW
BEGIN
  SET NEW.last_updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER trg_comments_after_insert
AFTER INSERT ON comments
FOR EACH ROW
BEGIN
  UPDATE posts SET last_updated_at = CURRENT_TIMESTAMP WHERE id = NEW.post_id;
END//

CREATE TRIGGER trg_posts_after_delete
AFTER DELETE ON posts
FOR EACH ROW
BEGIN
  INSERT INTO post_audit_logs(post_id, action, reason)
  VALUES (OLD.id, 'POST_DELETED', 'post removed from system');
END//

DELIMITER ;
