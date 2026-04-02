# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

LLM-Travel-Advisor 是一个基于 Spring Boot 3 的智能旅游景点推荐平台。当前处于早期开发阶段，认证模块已完成，核心业务模块（景点、评论、行程、推荐、AI 对话）待实现。

## 技术栈

- **后端**: Spring Boot 3.3.4 + Java 17 + MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0, Redis
- **认证**: JWT (JJWT 0.12.6) + Spring Security 6
- **API 文档**: SpringDoc OpenAPI 3.0 (Swagger UI)
- **文件存储**: MinIO
- **构建工具**: Maven

## 开发与运行命令

### 构建
```bash
mvn clean install              # 完整构建
mvn clean package -DskipTests  # 跳过测试打包
```

### 运行
```bash
mvn spring-boot:run                                        # 开发环境运行
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"  # 生产环境
java -jar target/llm-travel-advisor-0.0.1-SNAPSHOT.jar     # 运行 JAR
```

### 测试
```bash
mvn test                    # 运行所有测试
mvn test -Dtest=ClassName   # 运行单个测试类
```

### 数据库初始化
```bash
mysql -uroot -p travel_recommend_system < travel_recommend_system.sql
```

### API 文档
启动后访问: http://localhost:8080/swagger-ui.html

## 项目结构

```
src/main/java/com/travel/advisor/
├── common/          # 通用组件 (Result<T>, 分页, 请求上下文)
├── config/          # 配置类 (JWT, Redis, MyBatis-Plus)
├── security/        # Spring Security 配置、JWT 过滤器、权限服务
├── controller/      # REST 控制器 (user/ admin/ common/ 分目录)
├── service/         # Service 接口及 impl/ 实现
├── mapper/          # MyBatis-Plus Mapper
├── entity/          # 数据库实体
├── dto/             # 请求 DTO
├── vo/              # 响应 VO
├── exception/       # 全局异常处理
├── utils/           # 工具类 (JwtUtils, RedisUtils, SecurityUtils 等)
└── filter/          # HTTP 过滤器 (RequestIdFilter)
```

## 关键约定

### 接口前缀
- 用户端: `/api/user/**`
- 管理端: `/api/admin/**`
- 公共: `/api/common/**`

### 统一响应格式
所有接口返回 `Result<T>`: `{ code, message, data, timestamp, requestId }`
自动包装由 `ResponseAdviceConfig` 实现，Controller 直接返回 `Result.success(data)` 即可。

### 分页规范
入参 `PageQuery` (pageNum, pageSize)，出参 `PageResult<T>` 使用 **Builder 模式** 构建:
```java
PageResult.<T>builder()
    .records(result.getRecords())
    .total(result.getTotal())
    .pageNum(Math.toIntExact(result.getCurrent()))
    .pageSize(Math.toIntExact(result.getSize()))
    .totalPage(result.getPages())
    .build();
```

### Bean 拷贝
使用 `BeanCopyUtils.copy(source, TargetClass.class)`，不是 `copyObject`。

### 认证机制
- JWT access token 2 小时过期, refresh token 7 天过期
- 用户和管理员使用独立的 token 体系
- 登出时将 token 加入 Redis 黑名单
- Security 配置在 `SecurityConfig.java`, JWT 过滤器在 `JwtAuthenticationFilter.java`

### 逻辑删除
所有表使用 `is_deleted` 字段 (0=未删除, 1=已删除), MyBatis-Plus 已配置全局逻辑删除

## 实现进度

**已完成**:
- ✅ 认证模块 (注册/登录/登出/刷新Token/重置密码/验证码)
- ✅ 地区模块 (用户端树形查询、管理端 CRUD)
- ✅ 标签模块 (用户端按类型查询、管理端 CRUD)

**待实现**: 用户管理、景点管理、收藏、浏览历史、点评、审核、行程计划、推荐引擎、AI 对话、文件上传、运营管理

## 注意事项

- `application-dev.yml` 中包含硬编码的数据库密码, 勿泄露
- 数据库密码: `wo221000`, Redis: `172.16.219.100:6379`
- `后端接口规范文档.md` 定义了完整的接口契约, 新增接口时需遵循文档规范
- 前端规划使用 Vue 3 + Vite + Naive UI, 尚未开始
- MyBatis-Plus 的 XML mapper 文件应放在 `resources/mapper/` 目录下 (配置在 `application.yml`)
