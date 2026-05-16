-- 修复实体与数据库不匹配的问题

-- users表添加收货地址字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100) COMMENT '邮箱' AFTER role;
ALTER TABLE users ADD COLUMN IF NOT EXISTS delivery_address VARCHAR(255) COMMENT '收货地址' AFTER email;
ALTER TABLE users ADD COLUMN IF NOT EXISTS delivery_name VARCHAR(50) COMMENT '收货人姓名' AFTER delivery_address;
ALTER TABLE users ADD COLUMN IF NOT EXISTS delivery_phone VARCHAR(20) COMMENT '收货人电话' AFTER delivery_name;
ALTER TABLE users ADD COLUMN IF NOT EXISTS province VARCHAR(50) COMMENT '省份' AFTER delivery_phone;
ALTER TABLE users ADD COLUMN IF NOT EXISTS city VARCHAR(50) COMMENT '城市' AFTER province;
ALTER TABLE users ADD COLUMN IF NOT EXISTS district VARCHAR(50) COMMENT '区县' AFTER city;

-- 1. carbon_points 表缺少 total_carbon 字段
ALTER TABLE carbon_points ADD COLUMN IF NOT EXISTS total_carbon DECIMAL(15,2) DEFAULT 0 COMMENT '累计碳减排量(kg)' AFTER used_points;

-- 2. forum_comments 表缺少 report_count 和 report_reason 字段
ALTER TABLE forum_comments ADD COLUMN IF NOT EXISTS report_count INT DEFAULT 0 COMMENT '举报次数' AFTER status;
ALTER TABLE forum_comments ADD COLUMN IF NOT EXISTS report_reason VARCHAR(500) COMMENT '举报原因' AFTER report_count;

-- 3. travel_records 表缺少 track_points 字段（如需存储轨迹）
ALTER TABLE travel_records ADD COLUMN IF NOT EXISTS track_points LONGTEXT COMMENT 'JSON格式轨迹点数据' AFTER travel_date;

-- 4. 创建 track_points 表（如不存在）
CREATE TABLE IF NOT EXISTS track_points (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  record_id BIGINT NOT NULL,
  latitude DECIMAL(10,6) COMMENT '纬度',
  longitude DECIMAL(10,6) COMMENT '经度',
  altitude DECIMAL(10,2) COMMENT '海拔(m)',
  speed DECIMAL(6,2) COMMENT '速度(m/s)',
  accuracy INT COMMENT '精度(m)',
  timestamp DATETIME COMMENT '时间戳',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_record_id (record_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
