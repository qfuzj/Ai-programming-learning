# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

LLM-Travel-Advisor 是一个基于 Spring Boot 3 的智能旅游景点推荐平台，包含完整的后端服务和前端应用。

## 参考文档

仓库根目录维护了几份设计文档，遇到对应主题先查这里再写代码：

- `后端接口规范文档.md` — 接口规范与请求/响应示例的权威来源
- `前端页面与路由设计.md` — 前端路由结构与页面职责的设计依据
- `travel_recommend_system.sql` — 数据库 DDL 与初始化脚本

## 技术栈

**后端**: Spring Boot 3.3.4 + Java 17 + MyBatis-Plus 3.5.7
**数据库**: MySQL 8.0, Redis
**认证**: JWT (JJWT 0.12.6) + Spring Security 6
**API 文档**: SpringDoc OpenAPI 3.0 (Swagger UI)
**文件存储**: MinIO（已实现，使用预签名 URL 上传）
**构建工具**: Maven (多模块项目)

**前端**: Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router + UnoCSS
**包管理**: pnpm

## 多模块结构

Maven 多模块结构，依赖关系: `travel-app` → `travel-web` → `travel-common` + `travel-model`

```
travel-recommend-platform (父 POM)
├── travel-common    # 通用组件: Result<T>, 分页, 异常处理, 工具类, JWT 工具
├── travel-model     # 数据模型: Entity 实体, MyBatis Mapper XML
├── travel-web       # Web 层: Controller, Service, DTO, VO, Security 配置
├── travel-app       # 启动模块: Spring Boot 主类, application.yml 配置
└── travel-ui        # 前端应用: Vue 3 + Vite + Element Plus
```

### 后端模块职责

| 模块 | 包路径 | 负责内容 |
|------|--------|----------|
| **travel-common** | `com.travel.advisor.common` | `Result<T>`, `PageQuery`, `PageResult`, `RequestContext` |
| | `com.travel.advisor.utils` | `BeanCopyUtils`, `JwtUtils`, `RedisUtils`, `SecurityUtils` |
| | `com.travel.advisor.exception` | `BusinessException`, `GlobalExceptionHandler` |
| | `com.travel.advisor.security` | `LoginUser` |
| **travel-model** | `com.travel.advisor.entity` | 数据库实体 (`User`, `ScenicSpot`, `Region` 等) |
| | `com.travel.advisor.mapper` | MyBatis-Plus Mapper 接口 |
| **travel-web** | `com.travel.advisor.controller` | REST 控制器 (admin/user/common 子包) |
| | `com.travel.advisor.service` | Service 接口及 impl 实现 |
| | `com.travel.advisor.dto` | 请求 DTO (按模块分子包) |
| | `com.travel.advisor.vo` | 响应 VO (按模块分子包) |
| | `com.travel.advisor.llm` | LLM 对话网关、提示词构建、上下文管理 |
| | `com.travel.advisor.recommend` | 推荐引擎: 召回策略、排名、理由生成 |
| | `com.travel.advisor.config` | Redis, MyBatis-Plus, ResponseAdvice 配置 |
| | `com.travel.advisor.security` | Spring Security 配置, `JwtAuthenticationFilter`, 权限服务 |
| | `com.travel.advisor.annotation` | 自定义注解 (如 `@OperationLog`) |
| | `com.travel.advisor.aspect` | AOP 切面 (如 `OperationLogAspect` 记录管理员操作) |
| | `com.travel.advisor.filter` | Servlet 过滤器 (如 `RequestIdFilter` 注入请求 ID) |
| | `com.travel.advisor.task` | 定时任务 (如 `FileCleanupTask` 清理无效文件) |
| **travel-app** | `com.travel.advisor` | `LLMTravelAdvisorApplication` 主启动类 |
| | `resources/` | `application.yml`, `application-dev.yml`, `application-prod.yml` |

### 前端结构

```
travel-ui/src/
├── api/           # API 请求封装 (axios)，按模块分文件
├── router/        # 路由配置 (user.ts, admin.ts) + 鉴权守卫
├── store/         # Pinia 状态管理 (userStore 等)
├── views/         # 页面组件
│   ├── admin/     # 管理端页面
│   ├── user/      # 用户端页面
│   └── auth/      # 认证页面 (登录/注册)
├── layouts/       # 布局组件 (AdminLayout, UserLayout)
├── components/    # 通用组件
├── utils/         # 工具函数 (request.ts - axios 封装含 token 刷新)
├── styles/        # 全局样式、SCSS 变量
└── types/         # TypeScript 类型定义
```

前端 API 调用使用 axios 实例 (`@/utils/request.ts`)，自动处理：
- 请求头注入 JWT token
- 401 响应时自动用 refresh token 刷新，失败则跳转登录页
- 统一响应格式解析 (`{ code, message, data }`)

## 开发与运行命令

### 后端

```bash
# 构建
mvn clean install                    # 完整构建 (从父目录执行)
mvn clean package -DskipTests        # 跳过测试打包

# 运行
mvn spring-boot:run -pl travel-app                                 # 开发环境
mvn spring-boot:run -pl travel-app -Dspring-boot.run.arguments="--spring.profiles.active=prod"  # 生产环境

# 测试
mvn test                                # 运行所有测试
mvn test -pl travel-app                 # 仅运行 travel-app 测试
mvn test -Dtest=ClassName              # 运行单个测试类

# 数据库初始化
mysql -uroot -p travel_recommend_system < travel_recommend_system.sql

# API 文档 (启动后访问)
# http://localhost:8080/swagger-ui.html
```

### 前端

```bash
cd travel-ui

# 安装依赖
pnpm install

# 开发
pnpm dev                    # 启动开发服务器 (http://localhost:3000)
                            # 代理到后端: VITE_APP_API_URL=http://localhost:8080
                            # 代理前缀: VITE_APP_BASE_API=/dev-api

# 构建
pnpm build                 # 类型检查 + 构建到 dist/

# 预览构建产物
pnpm preview

# 代码检查
pnpm lint                  # eslint + prettier + stylelint
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
入参 `PageQuery` (pageNum, pageSize)，出参 `PageResult<T>` 使用 Builder 模式构建:
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
- 用户和管理员使用独立的 token 体系 (token 刷新 URL 不同)
- 登出时将 token 加入 Redis 黑名单
- Security 配置在 `SecurityConfig.java`, JWT 过滤器在 `JwtAuthenticationFilter.java`
- 前端 request.ts 自动处理 token 刷新逻辑

### 逻辑删除
所有表使用 `is_deleted` 字段 (0=未删除, 1=已删除), MyBatis-Plus 已配置全局逻辑删除。

### 文件上传
MinIO 已集成，使用预签名 URL 模式：
1. 前端调用 `/api/common/file/upload-url` 获取 PUT 预签名 URL
2. 前端直接 PUT 上传文件到 MinIO (不通过后端)
3. 上传完成后调用 `/api/common/file/confirm` 确认

### 前端开发备注
- 路由守卫 (`router/guards/auth.ts`) 处理认证和权限
- 自动导入已配置: Vue API、Element Plus 组件/函数、Pinia、Vue Router
- 使用 UnoCSS 实用类 (非 Tailwind，配置在 `uno.config.ts`)
- 组件自动导入 (unplugin-vue-components)，无需手动 import Element Plus 组件

## 实现进度

后端功能模块基本完成，覆盖认证、地区、标签、景点、收藏、浏览历史、点评、审核、用户管理、行程计划、推荐系统、LLM 对话、系统配置、操作日志、统计看板、文件上传等模块。前端用户端与管理端页面已完整实现。具体接口清单见 `后端接口规范文档.md`。

## 已知问题

### `syncImages` 方法会覆盖已有的图片 URL (P0)
- **问题**: `ScenicSpotServiceImpl.syncImages()` 先将景点所有图片记录清空, 再插入新记录时 `imageUrl` 置为空字符串。
- **原因**: `ScenicCreateDTO`/`ScenicUpdateDTO` 的 `imageIds` 只传了文件资源 ID, 没有 URL 信息。
- **临时方案**: 景点图片管理使用 `addImage`/`listImages`/`deleteImage` 独立接口, 不依赖 `create`/`update` 中的 `imageIds` 同步。

## 开发注意事项

- 新增 Entity/Mapper 放在 **travel-model** 模块
- 新增 Controller/Service/DTO/VO 放在 **travel-web** 模块
- 新增通用工具类/组件放在 **travel-common** 模块
- 修改配置只在 **travel-app** 的 `application*.yml`
- 跨模块依赖: travel-web 依赖 travel-common + travel-model, travel-app 依赖 travel-web
- 密码已通过环境变量引用 (`DB_PASSWORD`, `REDIS_PASSWORD`), 生产环境应覆盖这些变量
- MyBatis-Plus 的 XML mapper 文件放在 `travel-model/src/main/resources/mapper/` 目录下
- 前端环境变量: `.env.development` (开发), `.env.production` (生产)
- 前端代理前缀: 开发环境 `/dev-api`, 生产环境 `/prod-api`
