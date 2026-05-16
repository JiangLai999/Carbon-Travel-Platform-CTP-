package com.carbon.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSchemaFixer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("检查并修复数据库表结构...");
        
        fixCarbonPointsTable();
        fixForumCommentsTable();
        fixTravelRecordsTable();
        fixActivitiesTable();
        fixUsersTable();
        createTrackPointsTableIfNotExists();
        createAddressesTableIfNotExists();
        
        log.info("数据库表结构检查完成");
    }

    private void fixCarbonPointsTable() {
        try {
            if (!columnExists("carbon_points", "total_carbon")) {
                jdbcTemplate.execute("ALTER TABLE carbon_points ADD COLUMN total_carbon DECIMAL(15,2) DEFAULT 0 COMMENT '累计碳减排量(kg)'");
                log.info("carbon_points 表已添加 total_carbon 字段");
            }
        } catch (Exception e) {
            log.warn("carbon_points 表修复失败: {}", e.getMessage());
        }
    }

    private void fixForumCommentsTable() {
        try {
            if (!columnExists("forum_comments", "report_count")) {
                jdbcTemplate.execute("ALTER TABLE forum_comments ADD COLUMN report_count INT DEFAULT 0 COMMENT '举报次数'");
                log.info("forum_comments 表已添加 report_count 字段");
            }
            if (!columnExists("forum_comments", "report_reason")) {
                jdbcTemplate.execute("ALTER TABLE forum_comments ADD COLUMN report_reason VARCHAR(500) COMMENT '举报原因'");
                log.info("forum_comments 表已添加 report_reason 字段");
            }
        } catch (Exception e) {
            log.warn("forum_comments 表修复失败: {}", e.getMessage());
        }
    }

    private void fixTravelRecordsTable() {
        try {
            if (!columnExists("travel_records", "track_points")) {
                jdbcTemplate.execute("ALTER TABLE travel_records ADD COLUMN track_points LONGTEXT COMMENT 'JSON格式轨迹点数据'");
                log.info("travel_records 表已添加 track_points 字段");
            }
        } catch (Exception e) {
            log.warn("travel_records 表修复失败: {}", e.getMessage());
        }
    }

    private void fixActivitiesTable() {
        try {
            if (!columnExists("activities", "type")) {
                jdbcTemplate.execute("ALTER TABLE activities ADD COLUMN type VARCHAR(50) DEFAULT '线下活动' COMMENT '活动类型'");
                log.info("activities 表已添加 type 字段");
            }
        } catch (Exception e) {
            log.warn("activities 表修复失败: {}", e.getMessage());
        }
    }

    private void fixUsersTable() {
        try {
            if (!columnExists("users", "email")) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN email VARCHAR(100) COMMENT '邮箱'");
                log.info("users 表已添加 email 字段");
            }
            if (!columnExists("users", "delivery_address")) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN delivery_address VARCHAR(255) COMMENT '收货地址'");
                log.info("users 表已添加 delivery_address 字段");
            }
            if (!columnExists("users", "delivery_name")) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN delivery_name VARCHAR(50) COMMENT '收货人姓名'");
                log.info("users 表已添加 delivery_name 字段");
            }
            if (!columnExists("users", "delivery_phone")) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN delivery_phone VARCHAR(20) COMMENT '收货人电话'");
                log.info("users 表已添加 delivery_phone 字段");
            }
            if (!columnExists("users", "province")) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN province VARCHAR(50) COMMENT '省份'");
                log.info("users 表已添加 province 字段");
            }
            if (!columnExists("users", "city")) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN city VARCHAR(50) COMMENT '城市'");
                log.info("users 表已添加 city 字段");
            }
            if (!columnExists("users", "district")) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN district VARCHAR(50) COMMENT '区县'");
                log.info("users 表已添加 district 字段");
            }
        } catch (Exception e) {
            log.warn("users 表修复失败: {}", e.getMessage());
        }
    }

    private void createTrackPointsTableIfNotExists() {
        try {
            if (!tableExists("track_points")) {
                jdbcTemplate.execute("""
                    CREATE TABLE track_points (
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
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
                log.info("track_points 表已创建");
            }
        } catch (Exception e) {
            log.warn("track_points 表创建失败: {}", e.getMessage());
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void createAddressesTableIfNotExists() {
        try {
            if (!tableExists("addresses")) {
                jdbcTemplate.execute("""
                    CREATE TABLE addresses (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        user_id BIGINT NOT NULL,
                        name VARCHAR(50) COMMENT '收货人姓名',
                        phone VARCHAR(20) COMMENT '联系电话',
                        province VARCHAR(50) COMMENT '省份',
                        city VARCHAR(50) COMMENT '城市',
                        district VARCHAR(50) COMMENT '区县',
                        detail_address VARCHAR(255) COMMENT '详细地址',
                        full_address VARCHAR(500) COMMENT '完整地址',
                        is_default TINYINT DEFAULT 0 COMMENT '是否为默认地址',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        INDEX idx_user_id (user_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
                log.info("addresses 表已创建");
            }
        } catch (Exception e) {
            log.warn("addresses 表创建失败: {}", e.getMessage());
        }
    }
}
