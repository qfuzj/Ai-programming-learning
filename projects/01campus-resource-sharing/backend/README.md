# 后端项目说明

校园闲置物资共享平台后端服务，基于 Spring Boot 3 构建，提供用户登录、商品发布、分类管理、订单、收藏、评价、举报、公告、轮播图和管理员后台等接口能力。

## 1. 项目简介

本模块是校园闲置物资共享平台的后端服务，采用前后端分离架构，为前端页面和管理员后台提供统一 API。

主要职责包括：

- 用户注册、登录、JWT 鉴权
- 商品发布、编辑、上下架、删除、详情查询
- 商品图片上传与静态资源访问
- 分类、轮播图、公告、举报、评论、订单管理
- 管理员后台的商品、用户、内容审核能力

## 2. 技术栈

- Java 17
- Spring Boot 3
- Spring Web / Spring Validation
- MyBatis-Plus
- MySQL 8
- JWT
- Lombok

## 3. 目录结构

```text
backend
├─ src/main/java/com/campus/resourcesharing
│  ├─ controller        # 接口控制层
│  ├─ service           # 业务接口
│  ├─ service/impl      # 业务实现
│  ├─ mapper            # MyBatis-Plus Mapper
│  ├─ entity            # 数据库实体
│  ├─ dto               # 请求参数对象
│  ├─ vo                # 响应视图对象
│  ├─ query             # 分页/筛选查询对象
│  ├─ config            # Web、MyBatis 等配置
│  ├─ interceptor       # 登录拦截器
│  ├─ utils             # 通用工具
│  └─ common            # 统一返回、异常处理等
├─ src/main/resources
│  ├─ application.yml   # 后端核心配置
│  └─ mapper            # XML 映射文件（如有）
└─ upload                # 本地上传目录，运行时自动生成
```

## 4. 启动要求

- JDK 17+
- Maven 3.8+
- MySQL 8+
- Node.js 18+（如需联调前端）

## 5. 初始化项目

### 5.1 创建数据库

先在 MySQL 中创建数据库，例如：

```sql
CREATE DATABASE campus_resource_sharing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 5.2 导入表结构

执行项目根目录下的 [数据库设计.sql](../数据库设计.sql) 初始化表结构和基础数据。

### 5.3 修改后端配置

编辑 [src/main/resources/application.yml](src/main/resources/application.yml)，至少确认以下配置：

- 数据库地址、用户名、密码
- `jwt.secret` 和 `jwt.expire-seconds`
- `server.port`
- 文件上传大小限制

当前默认配置示例：

```yml
server:
	port: 8080

spring:
	datasource:
		url: jdbc:mysql://localhost:3306/campus_resource_sharing?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
		username: root
		password: your-password
	servlet:
		multipart:
			max-file-size: 10MB
			max-request-size: 10MB

jwt:
	secret: ReplaceWithYourOwnSecretKeyAtLeast32CharsLong
	expire-seconds: 86400
```

### 5.4 检查上传目录

后端商品图片、轮播图、头像等资源会写入项目根目录下的 `upload/` 目录，启动后如果目录不存在会自动创建。

## 6. 启动步骤

### 6.1 本地启动后端

在 `backend` 目录下执行：

```bash
mvn clean install
mvn spring-boot:run
```

启动成功后，后端默认地址为：

```text
http://localhost:8080
```

### 6.2 联调前端（可选）

如果需要同时启动前端，在 `frontend` 目录下执行：

```bash
npm install
npm run dev
```

前端默认地址为：

```text
http://localhost:5173
```

如前端通过环境变量配置后端地址，请在前端 `.env.development` 中设置：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 7. 配置说明

### 7.1 数据库配置

`application.yml` 中的 `spring.datasource` 负责数据库连接。请按本地环境调整：

- `url`：MySQL JDBC 地址
- `username`：数据库用户名
- `password`：数据库密码

### 7.2 JWT 配置

- `jwt.secret`：JWT 签名密钥，建议替换为不少于 32 位的随机字符串
- `jwt.expire-seconds`：Token 过期时间，单位秒

### 7.3 文件上传配置

当前后端默认允许上传 10MB 以内的图片文件，并通过 `upload/**` 资源映射对外访问：

- 图片上传接口：`POST /api/upload/image`
- 访问路径示例：`/upload/goods/xxx.jpg`

如果部署到生产环境，建议将上传目录迁移到对象存储服务，例如 OSS 或 MinIO。

## 8. 常用接口

### 8.1 基础接口

- `GET /api/common/ping`

### 8.2 首页接口

- `GET /api/home/banners`
- `GET /api/home/recommend`
- `GET /api/home/latest`
- `GET /api/home/notices`
- `GET /api/home/categories`

### 8.3 商品接口

- `GET /api/goods/list`
- `GET /api/goods/detail/{id}`
- `POST /api/goods/add`
- `PUT /api/goods/update`
- `DELETE /api/goods/delete/{id}`

### 8.4 管理员接口

- `GET /api/admin/goods/page`
- `GET /api/admin/goods/detail/{id}`
- `PUT /api/admin/goods/status/{id}`
- `DELETE /api/admin/goods/delete/{id}`
- `GET /api/admin/banner/page`
- `POST /api/admin/banner/add`
- `PUT /api/admin/banner/update`

## 9. 默认账号说明

如果项目初始化脚本中写入了管理员账号，请在导入数据库后自行检查并重置密码。生产环境不要直接使用默认口令。

## 10. 开发建议

推荐按以下顺序开发和排查问题：

1. 数据库初始化
2. 登录和鉴权
3. 商品发布与详情
4. 图片上传与静态资源访问
5. 首页聚合数据
6. 管理后台列表与审核操作

## 11. 常见问题

### 11.1 图片上传失败

优先检查：

- 文件是否超过 10MB
- `multipart` 配置是否生效
- `upload/` 目录是否有写入权限
- 前端请求是否使用了正确的 `VITE_API_BASE_URL`

### 11.2 图片可以上传但页面看不到

优先检查：

- 数据库中是否保存了正确的相对路径，例如 `/upload/goods/xxx.jpg`
- `WebMvcConfig` 中是否正确映射了 `/upload/**`
- 前端是否使用了 `resolveFileUrl` 进行路径补全

### 11.3 登录后接口 401

优先检查：

- 前端是否已写入最新 token
- `jwt.secret` 是否与历史 token 一致
- Token 是否已经过期

## 12. 备注

本 README 仅描述后端服务。如果需要完整的项目说明，请同时参考根目录下的 [README.md](../README.md) 和配套设计文档。
