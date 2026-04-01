# backend

Spring Boot 3 + MyBatis-Plus + MySQL + JWT 基础骨架。

## 启动要求

- JDK 17+
- Maven 3.8+
- MySQL 8+

## 启动步骤

1. 修改 `src/main/resources/application.yml` 中数据库连接
2. 执行数据库脚本（项目根目录 `数据库设计.sql`）
3. 启动项目：

```bash
mvn spring-boot:run
```

## 基础接口

- GET /api/common/ping
