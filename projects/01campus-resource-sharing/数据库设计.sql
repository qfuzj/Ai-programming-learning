-- =========================================
-- 数据库：campus_resource_sharing
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_general_ci
-- =========================================

CREATE DATABASE IF NOT EXISTS campus_resource_sharing
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE campus_resource_sharing;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================
-- 1. 用户表
-- =========================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                          username VARCHAR(50) NOT NULL COMMENT '用户名',
                          password VARCHAR(255) NOT NULL COMMENT '加密密码',
                          nickname VARCHAR(50) NOT NULL COMMENT '昵称',
                          avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
                          phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                          email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                          gender TINYINT DEFAULT 0 COMMENT '性别：0未知 1男 2女',
                          college VARCHAR(100) DEFAULT NULL COMMENT '学院',
                          role VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：user/admin',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_username (username),
                          KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =========================================
-- 2. 商品分类表
-- =========================================
DROP TABLE IF EXISTS goods_category;
CREATE TABLE goods_category (
                                id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                name VARCHAR(50) NOT NULL COMMENT '分类名称',
                                sort INT NOT NULL DEFAULT 0 COMMENT '排序',
                                status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- =========================================
-- 3. 商品表
-- =========================================
DROP TABLE IF EXISTS goods_info;
CREATE TABLE goods_info (
                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                            user_id BIGINT NOT NULL COMMENT '发布者ID',
                            category_id BIGINT NOT NULL COMMENT '分类ID',
                            title VARCHAR(100) NOT NULL COMMENT '商品标题',
                            description TEXT NOT NULL COMMENT '商品描述',
                            price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价',
                            original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
                            condition_level VARCHAR(20) DEFAULT NULL COMMENT '成色',
                            contact_info VARCHAR(100) NOT NULL COMMENT '联系方式',
                            trade_location VARCHAR(100) DEFAULT NULL COMMENT '交易地点',
                            main_image VARCHAR(255) DEFAULT NULL COMMENT '主图',
                            view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
                            favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏量',
                            status VARCHAR(20) NOT NULL DEFAULT 'on_sale' COMMENT '状态：pending/on_sale/sold/off_shelf/rejected',
                            deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (id),
                            KEY idx_user_id (user_id),
                            KEY idx_category_id (category_id),
                            KEY idx_status (status),
                            KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- =========================================
-- 4. 商品图片表
-- =========================================
DROP TABLE IF EXISTS goods_image;
CREATE TABLE goods_image (
                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
                             goods_id BIGINT NOT NULL COMMENT '商品ID',
                             image_url VARCHAR(255) NOT NULL COMMENT '图片地址',
                             sort INT NOT NULL DEFAULT 0 COMMENT '排序',
                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (id),
                             KEY idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- =========================================
-- 5. 收藏表
-- =========================================
DROP TABLE IF EXISTS goods_favorite;
CREATE TABLE goods_favorite (
                                id BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
                                user_id BIGINT NOT NULL COMMENT '用户ID',
                                goods_id BIGINT NOT NULL COMMENT '商品ID',
                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_user_goods (user_id, goods_id),
                                KEY idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

-- =========================================
-- 6. 订单表
-- =========================================
DROP TABLE IF EXISTS goods_order;
CREATE TABLE goods_order (
                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                             order_no VARCHAR(50) NOT NULL COMMENT '订单编号',
                             goods_id BIGINT NOT NULL COMMENT '商品ID',
                             buyer_id BIGINT NOT NULL COMMENT '买家ID',
                             seller_id BIGINT NOT NULL COMMENT '卖家ID',
                             deal_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '成交价格',
                             status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/confirmed/trading/completed/cancelled/closed',
                             remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_order_no (order_no),
                             KEY idx_goods_id (goods_id),
                             KEY idx_buyer_id (buyer_id),
                             KEY idx_seller_id (seller_id),
                             KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =========================================
-- 7. 订单日志表
-- =========================================
DROP TABLE IF EXISTS goods_order_log;
CREATE TABLE goods_order_log (
                                 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                 order_id BIGINT NOT NULL COMMENT '订单ID',
                                 action VARCHAR(50) NOT NULL COMMENT '操作类型',
                                 operator_id BIGINT NOT NULL COMMENT '操作人ID',
                                 content VARCHAR(255) NOT NULL COMMENT '日志内容',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (id),
                                 KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单日志表';

-- =========================================
-- 8. 留言消息表
-- =========================================
DROP TABLE IF EXISTS goods_message;
CREATE TABLE goods_message (
                               id BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                               goods_id BIGINT DEFAULT NULL COMMENT '商品ID',
                               sender_id BIGINT NOT NULL COMMENT '发送人ID',
                               receiver_id BIGINT NOT NULL COMMENT '接收人ID',
                               order_id BIGINT DEFAULT NULL COMMENT '订单ID',
                               content VARCHAR(500) NOT NULL COMMENT '消息内容',
                               message_type VARCHAR(20) NOT NULL DEFAULT 'consult' COMMENT '消息类型：consult/system/order',
                               is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0否 1是',
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               PRIMARY KEY (id),
                               KEY idx_sender_id (sender_id),
                               KEY idx_receiver_id (receiver_id),
                               KEY idx_goods_id (goods_id),
                               KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言消息表';

-- =========================================
-- 9. 公告表
-- =========================================
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
                            title VARCHAR(100) NOT NULL COMMENT '公告标题',
                            content TEXT NOT NULL COMMENT '公告内容',
                            status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1显示 0隐藏',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- =========================================
-- 10. 轮播图表
-- =========================================
DROP TABLE IF EXISTS sys_banner;
CREATE TABLE sys_banner (
                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
                            title VARCHAR(100) DEFAULT NULL COMMENT '标题',
                            image_url VARCHAR(255) NOT NULL COMMENT '图片地址',
                            link_url VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
                            sort INT NOT NULL DEFAULT 0 COMMENT '排序',
                            status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- =========================================
-- 11. 举报表
-- =========================================
DROP TABLE IF EXISTS goods_report;
CREATE TABLE goods_report (
                              id BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
                              reporter_id BIGINT NOT NULL COMMENT '举报人ID',
                              target_type VARCHAR(20) NOT NULL COMMENT '举报对象类型：goods/user',
                              target_id BIGINT NOT NULL COMMENT '举报对象ID',
                              reason VARCHAR(255) NOT NULL COMMENT '举报原因',
                              description VARCHAR(500) DEFAULT NULL COMMENT '详细描述',
                              status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
                              handle_result VARCHAR(255) DEFAULT NULL COMMENT '处理结果',
                              create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (id),
                              KEY idx_reporter_id (reporter_id),
                              KEY idx_target_id (target_id),
                              KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- =========================================
-- 12. 评价表
-- =========================================
DROP TABLE IF EXISTS goods_comment;
CREATE TABLE goods_comment (
                               id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
                               order_id BIGINT NOT NULL COMMENT '订单ID',
                               goods_id BIGINT NOT NULL COMMENT '商品ID',
                               from_user_id BIGINT NOT NULL COMMENT '评价人ID',
                               to_user_id BIGINT NOT NULL COMMENT '被评价人ID',
                               score TINYINT NOT NULL DEFAULT 5 COMMENT '评分：1-5',
                               content VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               PRIMARY KEY (id),
                               KEY idx_order_id (order_id),
                               KEY idx_goods_id (goods_id),
                               KEY idx_from_user_id (from_user_id),
                               KEY idx_to_user_id (to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- =========================================
-- 初始化分类数据
-- =========================================
INSERT INTO goods_category (name, sort, status) VALUES
                                                    ('教材书籍', 1, 1),
                                                    ('数码电子', 2, 1),
                                                    ('生活用品', 3, 1),
                                                    ('服饰鞋包', 4, 1),
                                                    ('体育器材', 5, 1),
                                                    ('宿舍神器', 6, 1),
                                                    ('其他', 99, 1);

-- =========================================
-- 初始化管理员账号
-- 用户名：admin
-- 密码建议后端启动后重新加密覆盖
-- 这里先写占位密文
-- =========================================
INSERT INTO sys_user (username, password, nickname, role, status)
VALUES ('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6s8GfK6N5mM8fK7rjWQ0qjvY0e8eK', '系统管理员', 'admin', 1);

SET FOREIGN_KEY_CHECKS = 1;