USE lab5_social;

CREATE TABLE IF NOT EXISTS friend_requests (
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
  CONSTRAINT chk_friend_requests_not_self CHECK (requester_id <> receiver_id),
  CONSTRAINT chk_friend_requests_status CHECK (status IN ('PENDING', 'ACCEPTED')),
  KEY idx_friend_requests_receiver_status (receiver_id, status, created_at),
  KEY idx_friend_requests_requester_receiver_status (requester_id, receiver_id, status)
);

SET @pair_index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'friend_requests'
    AND index_name = 'idx_friend_requests_requester_receiver_status'
);
SET @create_pair_index_sql = IF(
  @pair_index_exists = 0,
  'CREATE INDEX idx_friend_requests_requester_receiver_status ON friend_requests(requester_id, receiver_id, status)',
  'SELECT 1'
);
PREPARE create_pair_index_stmt FROM @create_pair_index_sql;
EXECUTE create_pair_index_stmt;
DEALLOCATE PREPARE create_pair_index_stmt;

SET @old_unique_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'friend_requests'
    AND index_name = 'uk_friend_requests_pair_status'
);
SET @drop_old_unique_sql = IF(
  @old_unique_exists > 0,
  'ALTER TABLE friend_requests DROP INDEX uk_friend_requests_pair_status',
  'SELECT 1'
);
PREPARE drop_old_unique_stmt FROM @drop_old_unique_sql;
EXECUTE drop_old_unique_stmt;
DEALLOCATE PREPARE drop_old_unique_stmt;
