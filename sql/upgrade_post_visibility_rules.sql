USE lab5_social;

CREATE TABLE IF NOT EXISTS post_visibility_rules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  rule_type VARCHAR(10) NOT NULL,
  target_type VARCHAR(10) NOT NULL,
  target_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_post_visibility_rules_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT chk_post_visibility_rules_rule_type CHECK (rule_type IN ('ALLOW', 'DENY')),
  CONSTRAINT chk_post_visibility_rules_target_type CHECK (target_type IN ('GROUP', 'USER')),
  CONSTRAINT uk_post_visibility_rules_target UNIQUE (post_id, rule_type, target_type, target_id)
);

SET @visibility_index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'post_visibility_rules'
    AND index_name = 'idx_post_visibility_rules_post'
);
SET @create_visibility_index_sql = IF(
  @visibility_index_exists = 0,
  'CREATE INDEX idx_post_visibility_rules_post ON post_visibility_rules(post_id, rule_type, target_type)',
  'SELECT 1'
);
PREPARE create_visibility_index_stmt FROM @create_visibility_index_sql;
EXECUTE create_visibility_index_stmt;
DEALLOCATE PREPARE create_visibility_index_stmt;
