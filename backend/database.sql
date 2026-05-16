-- 低碳出行激励平台数据库脚本

CREATE DATABASE IF NOT EXISTS carbon_platform DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE carbon_platform;

-- 用户表
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号',
  password VARCHAR(255) NOT NULL COMMENT '密码',
  nickname VARCHAR(100) COMMENT '昵称',
  avatar VARCHAR(255) COMMENT '头像URL',
  real_name VARCHAR(100) COMMENT '真实姓名',
  id_card VARCHAR(20) COMMENT '身份证号',
  status TINYINT DEFAULT 1 COMMENT '状态: 1正常 0禁用',
  role VARCHAR(20) DEFAULT 'user' COMMENT '角色: user普通用户 admin管理员',
  email VARCHAR(100) COMMENT '邮箱',
  delivery_address VARCHAR(255) COMMENT '收货地址',
  delivery_name VARCHAR(50) COMMENT '收货人姓名',
  delivery_phone VARCHAR(20) COMMENT '收货人电话',
  province VARCHAR(50) COMMENT '省份',
  city VARCHAR(50) COMMENT '城市',
  district VARCHAR(50) COMMENT '区县',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_phone (phone),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 出行方式表
CREATE TABLE travel_modes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE COMMENT '出行方式名称',
  icon VARCHAR(255) COMMENT '图标URL',
  carbon_reduction DECIMAL(10,2) NOT NULL COMMENT '单位碳减排量(kg/km)',
  points_per_km DECIMAL(10,2) NOT NULL COMMENT '每公里获得积分',
  max_points_per_trip DECIMAL(10,2) DEFAULT 100 COMMENT '单次最高积分',
  sort_order INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 出行记录表
CREATE TABLE travel_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  travel_mode_id BIGINT NOT NULL,
  start_location VARCHAR(255) NOT NULL COMMENT '起点',
  end_location VARCHAR(255) NOT NULL COMMENT '终点',
  distance DECIMAL(10,2) NOT NULL COMMENT '距离(km)',
  carbon_reduction DECIMAL(10,2) NOT NULL COMMENT '碳减排量(kg)',
  points_earned DECIMAL(10,2) DEFAULT 0 COMMENT '获得积分',
  status TINYINT DEFAULT 0 COMMENT '状态: 0待审核 1已通过 2已驳回',
  review_comment VARCHAR(500) COMMENT '审核意见',
  reviewed_by BIGINT COMMENT '审核人ID',
  reviewed_at TIMESTAMP NULL COMMENT '审核时间',
  travel_date DATE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (travel_mode_id) REFERENCES travel_modes(id),
  INDEX idx_user_id (user_id),
  INDEX idx_status (status),
  INDEX idx_travel_date (travel_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 碳积分表
CREATE TABLE carbon_points (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  total_points DECIMAL(15,2) DEFAULT 0 COMMENT '总积分',
  available_points DECIMAL(15,2) DEFAULT 0 COMMENT '可用积分',
  used_points DECIMAL(15,2) DEFAULT 0 COMMENT '已使用积分',
  total_carbon DECIMAL(15,4) DEFAULT 0 COMMENT '累计碳减排量(kg)',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 积分明细表
CREATE TABLE points_details (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  points DECIMAL(15,2) NOT NULL COMMENT '积分数量',
  type VARCHAR(50) NOT NULL COMMENT '类型: travel出行 activity活动 purchase购物 exchange兑换',
  source_id BIGINT COMMENT '来源ID(出行记录ID/活动ID等)',
  description VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品分类表
CREATE TABLE product_categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  code VARCHAR(50) UNIQUE NOT NULL COMMENT '分类编码',
  icon VARCHAR(255) COMMENT '图标URL',
  description VARCHAR(255) COMMENT '分类描述',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_code (code),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品表
CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  image_url VARCHAR(255),
  category VARCHAR(50) COMMENT '分类编码',
  points_required DECIMAL(15,2) NOT NULL COMMENT '所需积分',
  stock INT DEFAULT 0 COMMENT '库存',
  carbon_label VARCHAR(100) COMMENT '低碳属性标签',
  status TINYINT DEFAULT 1 COMMENT '1上架 0下架',
  sort_order INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category (category),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单表
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50) UNIQUE NOT NULL,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT DEFAULT 1,
  points_spent DECIMAL(15,2) NOT NULL,
  status TINYINT DEFAULT 0 COMMENT '0待发货 1已发货 2已收货 3已取消',
  delivery_address VARCHAR(255),
  delivery_no VARCHAR(100) COMMENT '快递单号',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (product_id) REFERENCES products(id),
  INDEX idx_user_id (user_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 活动表
CREATE TABLE activities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  description TEXT,
  image_url VARCHAR(255),
  type VARCHAR(50) COMMENT '活动类型: 线下活动 线上活动 线上讲座 线下峰会 线上工作坊',
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  requirement VARCHAR(255) COMMENT '参与要求',
  reward_points DECIMAL(15,2) NOT NULL COMMENT '奖励积分',
  status TINYINT DEFAULT 1 COMMENT '1进行中 0已结束',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 活动参与表
CREATE TABLE activity_participations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  evidence_url VARCHAR(255) COMMENT '参与凭证URL',
  status TINYINT DEFAULT 0 COMMENT '0待审核 1已通过 2已驳回',
  review_comment VARCHAR(500),
  reviewed_by BIGINT,
  reviewed_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (activity_id) REFERENCES activities(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  UNIQUE KEY unique_participation (activity_id, user_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 论坛板块表
CREATE TABLE forum_sections (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(255),
  icon VARCHAR(255),
  sort_order INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 论坛帖子表
CREATE TABLE forum_posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  section_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content LONGTEXT NOT NULL,
  images VARCHAR(1000) COMMENT 'JSON数组存储图片URL',
  views INT DEFAULT 0,
  likes INT DEFAULT 0,
  comments_count INT DEFAULT 0,
  is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
  status TINYINT DEFAULT 1 COMMENT '1正常 0删除',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (section_id) REFERENCES forum_sections(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_section_id (section_id),
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 论坛评论表
CREATE TABLE forum_comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content VARCHAR(1000) NOT NULL,
  likes INT DEFAULT 0,
  status TINYINT DEFAULT 1 COMMENT '1正常 0删除',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (post_id) REFERENCES forum_posts(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_post_id (post_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户关注表
CREATE TABLE user_follows (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  follower_id BIGINT NOT NULL,
  following_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (follower_id) REFERENCES users(id),
  FOREIGN KEY (following_id) REFERENCES users(id),
  UNIQUE KEY unique_follow (follower_id, following_id),
  INDEX idx_follower_id (follower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 私信表
CREATE TABLE messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  content VARCHAR(1000) NOT NULL,
  is_read TINYINT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (sender_id) REFERENCES users(id),
  FOREIGN KEY (receiver_id) REFERENCES users(id),
  INDEX idx_receiver_id (receiver_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 公告表
CREATE TABLE announcements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content LONGTEXT NOT NULL,
  status TINYINT DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化出行方式
INSERT INTO travel_modes (name, carbon_reduction, points_per_km, sort_order) VALUES
('步行', 0.0, 5, 1),
('骑行', 0.21, 10, 2),
('公交', 0.08, 8, 3),
('地铁', 0.04, 7, 4),
('电动车', 0.05, 6, 5);

-- 初始化论坛板块
INSERT INTO forum_sections (name, description, sort_order) VALUES
('出行分享', '分享你的低碳出行故事', 1),
('路线攻略', '推荐优质出行路线', 2),
('低碳问答', '讨论低碳出行相关问题', 3);

-- 初始化商品分类
INSERT INTO product_categories (name, code, description, sort_order, status) VALUES
('自行车', 'bike', '各类自行车及骑行装备', 1, 1),
('装备', 'equipment', '骑行安全装备', 2, 1),
('配件', 'accessories', '骑行配件及周边', 3, 1),
('环保用品', 'eco', '环保生活用品', 4, 1),
('其他', 'other', '其他商品', 99, 1);

-- 商品测试数据
INSERT IGNORE INTO products (name, description, category, points_required, stock, carbon_label, status, sort_order) VALUES
('折叠自行车', '轻便折叠，通勤首选，低碳出行好伴侣', 'bike', 500, 10, '低碳环保', 1, 1),
('智能骑行头盔', '骑行安全必备，内置LED灯', 'equipment', 200, 20, '安全骑行', 1, 2),
('防晒骑行服', '透气速干，UV防护', 'accessories', 150, 30, '环保材质', 1, 3),
('便携雨衣', '折叠收纳，随身携带', 'accessories', 80, 50, '可回收材料', 1, 4),
('铝合金水壶架', '轻量化设计，通用型', 'accessories', 60, 100, '铝合金环保', 1, 5);

-- 活动测试数据
INSERT IGNORE INTO activities (title, description, start_date, end_date, requirement, reward_points, status) VALUES
('绿色出行月', '参与本月低碳出行挑战，累计骑行50km即可获得奖励', '2026-03-01', '2026-03-31', '累计骑行50km', 100, 1),
('无车日挑战', '选择公共交通或自行车出行，记录一天的绿色出行', '2026-03-15', '2026-03-22', '记录一天出行', 50, 1),
('低碳知识竞赛', '参与低碳知识问答，答对8题以上获得积分奖励', '2026-03-10', '2026-03-20', '答对8题以上', 30, 1);

-- 公告测试数据
INSERT IGNORE INTO announcements (title, content, status) VALUES
('平台上线公告', '低碳出行激励平台正式上线！欢迎大家积极参与，完成出行记录即可获得碳积分。', 1),
('积分兑换规则', '商城积分兑换已全面开放，快来用积分换好物！积分永久有效，放心积累。', 1);

-- 管理员账号 (手机号: 13800138000, 密码: admin123)
-- BCrypt加密后的密码: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E (实际使用时请用真实hash)
INSERT IGNORE INTO users (phone, password, nickname, role, status) VALUES
('13800138000', '$2a$10$XYZabc123456789012345678901234567890123456789012345', '系统管理员', 'admin', 1);

-- 为管理员初始化碳积分
INSERT IGNORE INTO carbon_points (user_id, total_points, available_points, used_points)
SELECT id, 0, 0, 0 FROM users WHERE phone = '13800138000';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
  config_value TEXT COMMENT '配置值',
  description VARCHAR(255) COMMENT '描述',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化防作弊配置
INSERT IGNORE INTO system_config (config_key, config_value, description) VALUES
('validity_type', 'rolling', '积分有效期类型: yearly/rolling/permanent'),
('holiday_multiplier', '2.0', '节假日奖励倍数'),
('daily_limit', '500', '每日积分获取上限'),
('max_distance_per_trip', '50', '单次出行最大距离限制(km)');
