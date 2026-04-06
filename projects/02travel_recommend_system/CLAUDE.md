# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

LLM-Travel-Advisor 是一个基于 Spring Boot 3 的智能旅游景点推荐平台。认证、地区、标签、景点、收藏、浏览历史、点评、审核模块已完成，待实现行程计划、推荐引擎、AI 对话、文件上传、统计看板等模块。

## 技术栈

- **后端**: Spring Boot 3.3.4 + Java 17 + MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0, Redis
- **认证**: JWT (JJWT 0.12.6) + Spring Security 6
- **API 文档**: SpringDoc OpenAPI 3.0 (Swagger UI)
- **文件存储**: MinIO
- **构建工具**: Maven (多模块项目)

## 多模块结构

项目采用 Maven 多模块结构，依赖关系: `travel-app` → `travel-web` → `travel-common` + `travel-model`

```
travel-recommend-platform (父 POM)
├── travel-common    # 通用组件: Result<T>, 分页, 异常处理, 工具类, JWT 工具, Security 基础
├── travel-model     # 数据模型: Entity 实体, MyBatis Mapper
├── travel-web       # Web 层: Controller, Service, DTO, VO, Security 配置, 过滤器
└── travel-app       # 启动模块: Spring Boot 主类, application.yml 配置
```

### 各模块职责

| 模块 | 包路径 | 负责内容 |
|------|--------|----------|
| **travel-common** | `com.travel.advisor.common` | `Result<T>`, `PageQuery`, `PageResult`, `RequestContext`, `ResultCode` |
| | `com.travel.advisor.config` | `JwtProperties` |
| | `com.travel.advisor.exception` | `BusinessException`, `GlobalExceptionHandler` |
| | `com.travel.advisor.security` | `LoginUser` |
| | `com.travel.advisor.utils` | `BeanCopyUtils`, `JsonUtils`, `JwtUtils`, `RedisUtils`, `SecurityUtils` |
| **travel-model** | `com.travel.advisor.entity` | 所有数据库实体 (`User`, `ScenicSpot`, `Region` 等) |
| | `com.travel.advisor.mapper` | MyBatis-Plus Mapper 接口 |
| | `resources/mapper/` | MyBatis XML mapper 文件 |
| **travel-web** | `com.travel.advisor.controller` | REST 控制器 (admin/user/common 子包) |
| | `com.travel.advisor.service` | Service 接口及 impl 实现 |
| | `com.travel.advisor.dto` | 请求 DTO (auth/history/region/review/scenic/tag 子包) |
| | `com.travel.advisor.vo` | 响应 VO (auth/favorite/history/region/review/scenic 子包) |
| | `com.travel.advisor.security` | Spring Security 配置, JWT 过滤器, 权限服务 |
| | `com.travel.advisor.config` | Redis, MyBatis-Plus, ResponseAdvice 配置 |
| | `com.travel.advisor.filter` | HTTP 过滤器 (RequestIdFilter) |
| **travel-app** | `com.travel.advisor` | `LLMTravelAdvisorApplication` 主启动类 |
| | `resources/` | `application.yml`, `application-dev.yml`, `application-prod.yml` |

### 开发注意事项

- 新增 Entity/Mapper 放在 **travel-model** 模块
- 新增 Controller/Service/DTO/VO 放在 **travel-web** 模块
- 新增通用工具类/组件放在 **travel-common** 模块
- 修改配置只在 **travel-app** 的 `application*.yml`
- 跨模块依赖: travel-web 依赖 travel-common + travel-model, travel-app 依赖 travel-web

## 开发与运行命令

### 构建
```bash
mvn clean install              # 完整构建 (从父目录执行)
mvn clean package -DskipTests  # 跳过测试打包
```

### 运行
```bash
mvn spring-boot:run -pl travel-app                                   # 开发环境运行
mvn spring-boot:run -pl travel-app -Dspring-boot.run.arguments="--spring.profiles.active=prod"  # 生产环境
java -jar travel-app/target/travel-app-0.0.1-SNAPSHOT.jar            # 运行 JAR
```

### 测试
```bash
mvn test                          # 运行所有测试 (从父目录执行)
mvn test -pl travel-app           # 仅运行 travel-app 测试
mvn test -Dtest=ClassName         # 运行单个测试类
```

### 数据库初始化
```bash
mysql -uroot -p travel_recommend_system < travel_recommend_system.sql
```

### API 文档
启动后访问: http://localhost:8080/swagger-ui.html

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
- ✅ 景点管理模块 (用户端分页/详情/筛选/热门榜, 管理端 CRUD/上下架/标签绑定/图片管理)
- ✅ 收藏模块 (收藏/取消收藏/收藏列表)
- ✅ 浏览历史模块 (上报/查询/删除/清空)
- ✅ 点评模块 (发布点评/我的点评/景点评价列表/删除点评)
- ✅ 审核模块 (review 点评类型审核列表/通过/拒绝/隐藏)

**待实现**: 用户管理、行程计划、推荐引擎、AI 对话、文件上传、运营管理、统计看板、操作日志

## 已知问题

### 审核模块仅完成 review 类型，其余类型未实现
- **现状**: `AuditServiceImpl.updateAuditAndReview()` 只处理 contentType 为 "review" 的审核。
  数据库中 `content_audit` 定义了 review/image/scenic/plan 四种类型，但目前仅 review 的发布 → 审核 → 回写状态流程完整。
- **待完善**: 实现 image/scenic/plan 的审核创建入口和状态回写策略。各类型的审核逻辑应独立为各自的方法，而不是硬塞进 `updateAuditAndReview`。

### `syncImages` 方法会覆盖已有的图片 URL (P0)
- **问题**: `ScenicSpotServiceImpl.syncImages()` 先将景点所有图片记录清空, 再插入新记录时 `imageUrl` 置为空字符串。如果之前通过 `addImage` 独立接口上传了带真实 URL 的图片, 调用 `create`/`update` 时会丢失图片 URL。
- **原因**: `ScenicCreateDTO`/`ScenicUpdateDTO` 的 `imageIds` 只传了文件资源 ID, 没有 URL 信息; 而 `file_resource` 表尚未实现, 无法反查 URL。
- **临时方案**: 景点图片管理使用 `addImage`/`listImages`/`deleteImage` 独立接口, 不依赖 `create`/`update` 中的 `imageIds` 同步。
- **彻底修复 (阶段十一完成)**: 创建 `FileResource` 实体和 Mapper 后, 将 `syncImages` 改为从 `file_resource` 表查出 URL 写入; 同时将 `create`/`update` 方法重新启用 `syncImages` 调用。

## 注意事项

- 密码已通过环境变量引用 (`DB_PASSWORD`, `REDIS_PASSWORD`), 生产环境应覆盖这些变量
- `后端接口规范文档.md` 定义了完整的接口契约, 新增接口时需遵循文档规范
- `AI分阶段生成代码任务清单.md` 定义了开发顺序, 新增模块时参考依赖关系
- 前端规划使用 Vue 3 + Vite + Naive UI, 尚未开始
- MyBatis-Plus 的 XML mapper 文件放在 `resources/mapper/` 目录下 (当前仅 `ScenicSpotTagMapper.xml`)
- 项目已配置 MinIO 支持 (见 `application.yml` 的 minio 配置段), 但服务层尚未实现
- pom.xml 中已引入 springdoc-openapi (v2.6.0), Swagger UI 在 http://localhost:8080/swagger-ui.html
