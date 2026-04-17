/*
 Navicat Premium Dump SQL

 Source Server         : DB
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : travel_recommend_system

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 15/04/2026 20:01:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt 加密）',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `role` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'operator' COMMENT '角色：super_admin / admin / operator',
  `permissions` text COLLATE utf8mb4_unicode_ci COMMENT '权限列表（JSON）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0 禁用 1 正常',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ----------------------------
-- Table structure for content_audit
-- ----------------------------
DROP TABLE IF EXISTS `content_audit`;
CREATE TABLE `content_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容类型：review / image / scenic / plan',
  `content_id` bigint NOT NULL COMMENT '内容 ID',
  `content_snapshot` text COLLATE utf8mb4_unicode_ci COMMENT '内容快照（JSON）',
  `submit_user_id` bigint DEFAULT NULL COMMENT '提交用户 ID',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态：0 待审核 1 通过 2 拒绝 3 人工复审',
  `auto_audit_result` text COLLATE utf8mb4_unicode_ci COMMENT '自动审核结果（JSON）',
  `auto_audit_score` decimal(5,4) DEFAULT NULL COMMENT '自动审核置信分',
  `llm_call_log_id` bigint DEFAULT NULL COMMENT 'LLM 审核调用记录 ID',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核员 ID（admin_user.id）',
  `audit_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `violation_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '违规类型（JSON 数组）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_type` (`content_type`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_submit_user_id` (`submit_user_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_auditor_id` (`auditor_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容审核表';

-- ----------------------------
-- Table structure for file_resource
-- ----------------------------
DROP TABLE IF EXISTS `file_resource`;
CREATE TABLE `file_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bucket_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MinIO bucket 名称',
  `object_key` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对象 key（路径）',
  `original_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始文件名',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型（MIME）',
  `file_extension` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件扩展名',
  `file_hash` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件 MD5 / SHA256（秒传 & 去重）',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '访问 URL',
  `thumbnail_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '缩略图 URL',
  `biz_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'default' COMMENT '业务类型（avatar / scenic / review 等）',
  `biz_id` bigint DEFAULT NULL COMMENT '业务 ID（反向关联）',
  `uploader_id` bigint DEFAULT NULL COMMENT '上传者 ID',
  `uploader_type` tinyint NOT NULL DEFAULT '1' COMMENT '上传者类型：1 用户 2 管理员 3 系统',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0 临时 1 已使用 2 已删除',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_object_key` (`bucket_name`,`object_key`),
  KEY `idx_bucket_name` (`bucket_name`),
  KEY `idx_biz_type` (`biz_type`),
  KEY `idx_biz_id` (`biz_id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_file_hash` (`file_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件资源表';

-- 迁移脚本：重置旧数据状态及使用时间
UPDATE file_resource SET status = 1 WHERE status = 1;
UPDATE file_resource SET status = 2 WHERE status = 2;
UPDATE file_resource SET status = 0 WHERE status = 0;
UPDATE file_resource SET used_time = create_time WHERE status = 1 AND used_time IS NULL;

-- ----------------------------
-- Table structure for llm_call_log
-- ----------------------------
DROP TABLE IF EXISTS `llm_call_log`;
CREATE TABLE `llm_call_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户 ID（系统调用可为空）',
  `call_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用类型：chat / recommend / analyze / audit',
  `model_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称（gpt‐4 / qwen‐max 等）',
  `provider` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务商（openai / aliyun / local）',
  `request_prompt` text COLLATE utf8mb4_unicode_ci COMMENT '请求 prompt',
  `request_messages` text COLLATE utf8mb4_unicode_ci COMMENT '完整请求（JSON）',
  `response_content` text COLLATE utf8mb4_unicode_ci COMMENT '响应内容',
  `input_tokens` int NOT NULL DEFAULT '0' COMMENT '输入 tokens',
  `output_tokens` int NOT NULL DEFAULT '0' COMMENT '输出 tokens',
  `total_tokens` int NOT NULL DEFAULT '0' COMMENT '总 tokens',
  `cost_amount` decimal(10,6) NOT NULL DEFAULT '0.000000' COMMENT '费用（元）',
  `response_time_ms` int DEFAULT NULL COMMENT '响应时间（毫秒）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0 失败 1 成功 2 超时 3 限流',
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `trace_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '链路追踪 ID',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求 IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_call_type` (`call_type`),
  KEY `idx_model_name` (`model_name`),
  KEY `idx_provider` (`provider`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_trace_id` (`trace_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM 调用日志表';

-- ----------------------------
-- Table structure for llm_conversation
-- ----------------------------
DROP TABLE IF EXISTS `llm_conversation`;
CREATE TABLE `llm_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '新对话' COMMENT '会话标题',
  `conversation_type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1 智能客服 2 行程规划 3 景点咨询',
  `context_data` text COLLATE utf8mb4_unicode_ci COMMENT '上下文数据（JSON，如当前景点等）',
  `message_count` int NOT NULL DEFAULT '0' COMMENT '消息数量',
  `total_tokens` int NOT NULL DEFAULT '0' COMMENT '消耗总 tokens',
  `last_message_at` datetime DEFAULT NULL COMMENT '最后消息时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0 关闭 1 进行中',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_conversation_type` (`conversation_type`),
  KEY `idx_last_message_at` (`last_message_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM 会话表';

-- ----------------------------
-- Table structure for llm_message
-- ----------------------------
DROP TABLE IF EXISTS `llm_message`;
CREATE TABLE `llm_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` bigint NOT NULL COMMENT '会话 ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色：user / assistant / system',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `content_type` tinyint NOT NULL DEFAULT '1' COMMENT '内容类型：1 文本 2 图片 3 卡片',
  `extra_data` text COLLATE utf8mb4_unicode_ci COMMENT '附加数据（JSON，如推荐景点列表）',
  `tokens_used` int DEFAULT '0' COMMENT '消耗 tokens',
  `llm_call_log_id` bigint DEFAULT NULL COMMENT '关联调用日志 ID',
  `is_sensitive` tinyint NOT NULL DEFAULT '0' COMMENT '是否敏感内容 0-非敏感，1-敏感内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_llm_call_log_id` (`llm_call_log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM 消息表';

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_user_id` bigint NOT NULL COMMENT '操作员 ID（admin_user.id）',
  `admin_username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作员账号（冗余）',
  `module` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作模块（scenic / user / review / config 等）',
  `action` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型（create / update / delete / audit / export 等）',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'HTTP 请求方法',
  `request_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求 URL',
  `request_params` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数（JSON，脱敏后存储）',
  `response_data` text COLLATE utf8mb4_unicode_ci COMMENT '响应数据（可选，较大时截断）',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP 地址',
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'User-Agent',
  `execution_time_ms` int DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0 失败 1 成功',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT '错误信息（失败时记录）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_user_id` (`admin_user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_action` (`action`),
  KEY `idx_ip_address` (`ip_address`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ----------------------------
-- Table structure for recommend_record
-- ----------------------------
DROP TABLE IF EXISTS `recommend_record`;
CREATE TABLE `recommend_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话 ID（游客 / 未登录）',
  `recommend_type` tinyint NOT NULL DEFAULT '1' COMMENT '推荐类型：1 首页 2 相似 3 行程 4 搜索',
  `scene` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐场景标识',
  `request_params` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数（JSON）',
  `user_profile_snapshot` text COLLATE utf8mb4_unicode_ci COMMENT '用户画像快照（JSON）',
  `algorithm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐算法标识',
  `llm_used` tinyint NOT NULL DEFAULT '0' COMMENT '是否使用 LLM',
  `llm_call_log_id` bigint DEFAULT NULL COMMENT '关联 LLM 调用记录 ID',
  `total_candidates` int DEFAULT NULL COMMENT '候选集总数',
  `returned_count` int DEFAULT NULL COMMENT '返回结果数',
  `response_time_ms` int DEFAULT NULL COMMENT '响应耗时（毫秒）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_recommend_type` (`recommend_type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_llm_call_log_id` (`llm_call_log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐记录表';

-- ----------------------------
-- Table structure for recommend_result_item
-- ----------------------------
DROP TABLE IF EXISTS `recommend_result_item`;
CREATE TABLE `recommend_result_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recommend_record_id` bigint NOT NULL COMMENT '推荐记录 ID',
  `scenic_spot_id` bigint NOT NULL COMMENT '推荐景点 ID',
  `rank_position` int NOT NULL DEFAULT '0' COMMENT '排名位置',
  `score` decimal(5,4) DEFAULT NULL COMMENT '推荐得分（0‐1）',
  `reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐理由（LLM 生成）',
  `is_clicked` tinyint NOT NULL DEFAULT '0' COMMENT '是否被点击',
  `is_favorited` tinyint NOT NULL DEFAULT '0' COMMENT '是否被收藏',
  `click_time` datetime DEFAULT NULL COMMENT '点击时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recommend_record_id` (`recommend_record_id`),
  KEY `idx_scenic_spot_id` (`scenic_spot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐结果明细表';

-- ----------------------------
-- Table structure for region
-- ----------------------------
DROP TABLE IF EXISTS `region`;
CREATE TABLE `region` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父级 ID（0 为顶级）',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地区名称',
  `short_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简称',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '层级：1 省 2 市 3 区县',
  `code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '行政区划代码',
  `pinyin` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拼音（搜索用）',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `is_hot` tinyint NOT NULL DEFAULT '0' COMMENT '是否热门城市',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_pinyin` (`pinyin`),
  KEY `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地区表';

-- ----------------------------
-- Table structure for review_like
-- ----------------------------
DROP TABLE IF EXISTS `review_like`;
CREATE TABLE `review_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `review_id` bigint NOT NULL COMMENT '点评ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_user` (`review_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点评点赞表';

-- ----------------------------
-- Table structure for review_reply
-- ----------------------------
DROP TABLE IF EXISTS `review_reply`;
CREATE TABLE `review_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `review_id` bigint NOT NULL COMMENT '点评ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点评回复表';

-- ----------------------------
-- Table structure for scenic_image
-- ----------------------------
DROP TABLE IF EXISTS `scenic_image`;
CREATE TABLE `scenic_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_spot_id` bigint NOT NULL COMMENT '景点 ID',
  `file_resource_id` bigint NOT NULL COMMENT '文件资源 ID（关联 file_resource）',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片 URL（冗余字段，方便直接读取）',
  `image_type` tinyint NOT NULL DEFAULT '1' COMMENT '图片类型：1 实景 2 地图 3 全景',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片标题 / 说明',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序（升序）',
  `is_cover` tinyint NOT NULL DEFAULT '0' COMMENT '是否封面图：0 否 1 是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  KEY `idx_scenic_spot_id` (`scenic_spot_id`),
  KEY `idx_file_resource_id` (`file_resource_id`),
  KEY `idx_image_type` (`image_type`),
  KEY `idx_is_cover` (`is_cover`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点图片表';

-- ----------------------------
-- Table structure for scenic_spot
-- ----------------------------
DROP TABLE IF EXISTS `scenic_spot`;
CREATE TABLE `scenic_spot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '景点名称',
  `region_id` bigint NOT NULL COMMENT '所属地区 ID',
  `address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '详细地址',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `cover_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图 URL',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '景点描述',
  `detail_content` longtext COLLATE utf8mb4_unicode_ci COMMENT '详细介绍（富文本）',
  `open_time` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开放时间说明',
  `ticket_info` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '门票信息',
  `ticket_price` decimal(10,2) DEFAULT NULL COMMENT '门票价格（元）',
  `level` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '景区等级：5A/4A/3A 等',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '自然风光' COMMENT '景点分类',
  `score` decimal(2,1) NOT NULL DEFAULT '0.0' COMMENT '综合评分（1‐5）',
  `rating_count` int NOT NULL DEFAULT '0' COMMENT '评分人数',
  `view_count` bigint NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `favorite_count` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `best_season` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最佳游玩季节',
  `suggested_hours` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '建议游玩时长',
  `tips` text COLLATE utf8mb4_unicode_ci COMMENT '游玩贴士',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0 下架 1 正常 2 审核中',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  `is_recommended` tinyint NOT NULL DEFAULT '0' COMMENT '是否平台推荐',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_region_id` (`region_id`),
  KEY `idx_category` (`category`),
  KEY `idx_level` (`level`),
  KEY `idx_view_count` (`view_count`),
  KEY `idx_status` (`status`),
  KEY `idx_is_recommended` (`is_recommended`),
  KEY `idx_ticket_price` (`ticket_price`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_location` (`longitude`,`latitude`),
  KEY `idx_score` (`score`),
  KEY `scenic_spot_id_index` (`id`),
  FULLTEXT KEY `ft_description` (`description`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点表';

-- ----------------------------
-- Table structure for scenic_spot_tag
-- ----------------------------
DROP TABLE IF EXISTS `scenic_spot_tag`;
CREATE TABLE `scenic_spot_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_spot_id` bigint NOT NULL COMMENT '景点 ID',
  `tag_id` bigint NOT NULL COMMENT '标签 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scenic_tag` (`scenic_spot_id`,`tag_id`),
  KEY `idx_scenic_spot_id` (`scenic_spot_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点-标签关联表';

-- ----------------------------
-- Table structure for stat_platform_daily
-- ----------------------------
DROP TABLE IF EXISTS `stat_platform_daily`;
CREATE TABLE `stat_platform_daily` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `new_user_count` int NOT NULL DEFAULT '0' COMMENT '新增用户数',
  `active_user_count` int NOT NULL DEFAULT '0' COMMENT '活跃用户数（当日有行为）',
  `total_pv` bigint NOT NULL DEFAULT '0' COMMENT '平台总 PV',
  `total_uv` int NOT NULL DEFAULT '0' COMMENT '平台总 UV',
  `scenic_view_count` int NOT NULL DEFAULT '0' COMMENT '景点浏览总次数',
  `favorite_count` int NOT NULL DEFAULT '0' COMMENT '收藏总次数',
  `review_count` int NOT NULL DEFAULT '0' COMMENT '评价总数',
  `travel_plan_count` int NOT NULL DEFAULT '0' COMMENT '创建行程数',
  `recommend_request_count` int NOT NULL DEFAULT '0' COMMENT '推荐请求数',
  `llm_call_count` int NOT NULL DEFAULT '0' COMMENT 'LLM 调用次数',
  `llm_cost_amount` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT 'LLM 费用（元）',
  `llm_total_tokens` bigint NOT NULL DEFAULT '0' COMMENT 'LLM 总 tokens',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台日统计表';

-- ----------------------------
-- Table structure for stat_scenic_daily
-- ----------------------------
DROP TABLE IF EXISTS `stat_scenic_daily`;
CREATE TABLE `stat_scenic_daily` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_spot_id` bigint NOT NULL COMMENT '景点 ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `pv_count` int NOT NULL DEFAULT '0' COMMENT '页面浏览量（PV）',
  `uv_count` int NOT NULL DEFAULT '0' COMMENT '独立访客数（UV）',
  `favorite_count` int NOT NULL DEFAULT '0' COMMENT '当日新增收藏数',
  `review_count` int NOT NULL DEFAULT '0' COMMENT '当日新增评价数',
  `avg_rating` decimal(2,1) DEFAULT NULL COMMENT '当日平均评分',
  `recommend_show_count` int NOT NULL DEFAULT '0' COMMENT '推荐曝光次数',
  `recommend_click_count` int NOT NULL DEFAULT '0' COMMENT '推荐点击次数',
  `search_count` int NOT NULL DEFAULT '0' COMMENT '搜索命中次数',
  `share_count` int NOT NULL DEFAULT '0' COMMENT '分享次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scenic_date` (`scenic_spot_id`,`stat_date`),
  KEY `idx_scenic_spot_id` (`scenic_spot_id`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点日统计表';

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键（唯一）',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT '配置值',
  `config_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'string' COMMENT '值类型：string / number / boolean / json',
  `config_group` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'default' COMMENT '配置分组（basic / llm / recommend / minio 等）',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置说明',
  `is_public` tinyint NOT NULL DEFAULT '0' COMMENT '是否前端可见：0 否 1 是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_group` (`config_group`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '标签类型：1 景点标签 2 用户偏好标签',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签分类（主题/风格/设施等）',
  `icon` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签图标',
  `color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签颜色（十六进制）',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0 禁用 1 正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_type` (`type`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ----------------------------
-- Table structure for travel_plan
-- ----------------------------
DROP TABLE IF EXISTS `travel_plan`;
CREATE TABLE `travel_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行程标题',
  `cover_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `total_days` int DEFAULT NULL COMMENT '总天数',
  `destination_region_id` bigint DEFAULT NULL COMMENT '目的地地区 ID',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '行程描述',
  `estimated_budget` decimal(10,2) DEFAULT NULL COMMENT '预算（元）',
  `travel_companion` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '同行人类型',
  `is_public` tinyint NOT NULL DEFAULT '0' COMMENT '是否公开',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `source` tinyint NOT NULL DEFAULT '1' COMMENT '来源：1 用户创建 2 LLM 生成 3 系统推荐',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0 草稿 1 正常 2 已完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_destination_region_id` (`destination_region_id`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行程计划表';

-- ----------------------------
-- Table structure for travel_plan_item
-- ----------------------------
DROP TABLE IF EXISTS `travel_plan_item`;
CREATE TABLE `travel_plan_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `travel_plan_id` bigint NOT NULL COMMENT '行程计划 ID',
  `scenic_spot_id` bigint DEFAULT NULL COMMENT '景点 ID（可选）',
  `day_number` int NOT NULL DEFAULT '1' COMMENT '第几天',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '当天内排序',
  `item_type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1 景点 2 餐饮 3 住宿 4 交通 5 自定义',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '详细说明',
  `start_time` time DEFAULT NULL COMMENT '开始时间',
  `end_time` time DEFAULT NULL COMMENT '结束时间',
  `location` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置 / 地址',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `estimated_cost` decimal(10,2) DEFAULT NULL COMMENT '预估费用',
  `notes` text COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  KEY `idx_travel_plan_id` (`travel_plan_id`),
  KEY `idx_scenic_spot_id` (`scenic_spot_id`),
  KEY `idx_day_sort` (`travel_plan_id`,`day_number`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行程明细表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt 加密）',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像 URL（MinIO）',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0 未知 1 男 2 女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `signature` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个性签名',
  `region_id` bigint DEFAULT NULL COMMENT '所在地区 ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0 禁用 1 正常',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录 IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_email` (`email`),
  KEY `idx_region_id` (`region_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for user_browse_history
-- ----------------------------
DROP TABLE IF EXISTS `user_browse_history`;
CREATE TABLE `user_browse_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `scenic_spot_id` bigint NOT NULL COMMENT '景点 ID',
  `duration_seconds` int DEFAULT '0' COMMENT '停留时长（秒）',
  `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '访问来源（search/recommend/share 等）',
  `device_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备类型',
  `browse_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_scenic_spot_id` (`scenic_spot_id`),
  KEY `idx_browse_time` (`browse_time`),
  KEY `idx_user_browse_time` (`user_id`,`browse_time`)
) ENGINE=InnoDB AUTO_INCREMENT=161 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `scenic_spot_id` bigint NOT NULL COMMENT '景点 ID',
  `folder_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '默认收藏' COMMENT '收藏夹名称',
  `remark` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收藏备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_scenic` (`user_id`,`scenic_spot_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_scenic_spot_id` (`scenic_spot_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ----------------------------
-- Table structure for user_preference_tag
-- ----------------------------
DROP TABLE IF EXISTS `user_preference_tag`;
CREATE TABLE `user_preference_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `tag_id` bigint NOT NULL COMMENT '标签 ID',
  `weight` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '偏好权重（0‐1）',
  `source` tinyint NOT NULL DEFAULT '1' COMMENT '来源：1 手动选择 2 行为分析 3 LLM 推断',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`,`tag_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好标签表';

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID（1:1）',
  `travel_style` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旅行风格（休闲/探险/文化等）',
  `budget_level` tinyint DEFAULT '2' COMMENT '预算水平：1 低 2 中 3 高',
  `preferred_season` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '偏好季节（JSON 数组）',
  `travel_companion` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出行同伴类型（独自/情侣/家庭等）',
  `interest_keywords` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '兴趣关键词（LLM 提取）',
  `embedding_vector` text COLLATE utf8mb4_unicode_ci COMMENT '画像向量（用于相似度计算）',
  `profile_version` int NOT NULL DEFAULT '1' COMMENT '画像版本号',
  `last_analyzed_at` datetime DEFAULT NULL COMMENT '最后分析时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户画像表';

-- ----------------------------
-- Table structure for user_review
-- ----------------------------
DROP TABLE IF EXISTS `user_review`;
CREATE TABLE `user_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `scenic_spot_id` bigint NOT NULL COMMENT '景点 ID',
  `rating` tinyint NOT NULL DEFAULT '5' COMMENT '评分（1‐5 星）',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '评价内容',
  `images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评价图片（JSON 数组）',
  `visit_date` date DEFAULT NULL COMMENT '游玩日期',
  `travel_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出行类型（独自/情侣/家庭等）',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `reply_count` int NOT NULL DEFAULT '0' COMMENT '回复数',
  `is_anonymous` tinyint NOT NULL DEFAULT '0' COMMENT '是否匿名',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0 待审核 1 通过 2 拒绝 3 隐藏',
  `audit_remark` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核备注',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发布 IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_scenic_spot_id` (`scenic_spot_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  FULLTEXT KEY `ft_content` (`content`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点评表';

SET FOREIGN_KEY_CHECKS = 1;
