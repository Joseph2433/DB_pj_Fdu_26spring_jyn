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
  CONSTRAINT uk_friend_requests_pair_status UNIQUE (requester_id, receiver_id, status),
  CONSTRAINT chk_friend_requests_not_self CHECK (requester_id <> receiver_id),
  CONSTRAINT chk_friend_requests_status CHECK (status IN ('PENDING', 'ACCEPTED')),
  KEY idx_friend_requests_receiver_status (receiver_id, status, created_at)
);
