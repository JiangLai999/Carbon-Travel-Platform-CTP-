-- 创建积分规则表
CREATE TABLE IF NOT EXISTS points_rules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  travel_mode_id BIGINT COMMENT '出行方式ID',
  points_per_km DECIMAL(10,2) COMMENT '每公里积分',
  carbon_reduction DECIMAL(10,4) COMMENT '每公里减碳量(kg)',
  status TINYINT DEFAULT 1 COMMENT '状态',
  description VARCHAR(500) COMMENT '描述',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_travel_mode (travel_mode_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';

-- 添加举报字段到评论表
ALTER TABLE forum_comments ADD COLUMN IF NOT EXISTS report_count INT DEFAULT 0 COMMENT '举报次数';
ALTER TABLE forum_comments ADD COLUMN IF NOT EXISTS report_reason VARCHAR(500) COMMENT '举报原因';
