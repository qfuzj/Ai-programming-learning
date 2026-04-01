# campus-resource-sharing

基于 **Spring Boot 3 + Vue 3** 的校园闲置物资共享平台，面向高校学生场景，提供闲置商品发布、浏览、收藏、咨询、下单、评价、举报及后台管理等功能。

---

## 1. 项目简介

`campus-resource-sharing` 是一个适用于校园内部二手转让场景的前后端分离项目，帮助学生高效处理闲置物品，提升资源利用率。

项目支持：
- 普通用户注册登录
- 闲置商品发布与管理
- 商品搜索、筛选、详情展示
- 收藏与留言咨询
- 轻量订单交易流程
- 评价与举报
- 管理员后台管理

---

## 2. 技术栈

## 前端
- Vue 3
- Vite
- Naive UI
- Pinia
- Vue Router
- Axios
- @vicons

## 后端
- Spring Boot 3
- MyBatis-Plus
- MySQL
- JWT
- Jakarta Validation

---

## 3. 项目特色

- 面向校园场景设计
- 采用轻量级交易流程
- 不依赖在线支付
- 前后端分离架构清晰
- 适合课程设计 / 毕业设计 / 项目实战
- 具备良好的扩展性

---

## 4. 功能模块

## 用户端
- 注册 / 登录 / 退出
- 首页展示
- 商品浏览、搜索、筛选
- 商品发布、编辑、下架、删除
- 收藏商品
- 留言咨询
- 创建订单 / 查看订单 / 完成交易
- 评价交易
- 举报违规商品或用户
- 个人中心

## 管理端
- 管理员登录
- 仪表盘统计
- 用户管理
- 商品管理
- 分类管理
- 订单管理
- 公告管理
- 轮播图管理
- 举报审核
- 评价管理

---

## 5. 项目结构

## 后端结构
```text
backend
├─ src/main/java/com/campus/resourcesharing
│  ├─ controller
│  ├─ service
│  ├─ service/impl
│  ├─ mapper
│  ├─ entity
│  ├─ dto
│  ├─ vo
│  ├─ query
│  ├─ config
│  ├─ interceptor
│  ├─ utils
│  └─ common
├─ src/main/resources
│  ├─ application.yml
│  └─ mapper
```

## 前端结构
```text
frontend
├─ src
│  ├─ api
│  ├─ assets
│  ├─ components
│  ├─ layout
│  ├─ router
│  ├─ store
│  ├─ utils
│  └─ views
```

---

## 6. 数据库初始化

1. 创建 MySQL 数据库
2. 执行 `数据库设计.sql`
3. 修改后端 `application.yml` 中的数据库连接配置

---

## 7. 后端启动说明

## 7.1 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.x

## 7.2 配置文件
修改 `application.yml`：

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_resource_sharing?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

## 7.3 启动步骤
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端默认启动后访问：
```text
http://localhost:8080
```

---

## 8. 前端启动说明

## 8.1 环境要求
- Node.js 18+
- npm / pnpm / yarn

## 8.2 安装依赖
```bash
cd frontend
npm install
```

## 8.3 可选环境变量
可在 `frontend/.env.development` 中配置后端地址：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 8.4 启动开发环境
```bash
npm run dev
```

前端默认访问：
```text
http://localhost:5173
```

---

## 9. 默认账号说明

## 管理员账号
- 用户名：`admin`
- 密码：请在后端初始化后自行重置或替换为加密后的安全密码

---

## 10. 页面设计风格

- 主题色：`#ffe60f`
- 文字主色：黑色
- 用户端：简洁、清新、校园风
- 管理端：规范、清晰、易操作

---

## 11. 开发文档说明

项目配套文档包括：
- `开发文档.md`
- `数据库设计.sql`
- `后端接口规范文档.md`
- `前端页面与路由设计.md`
- `AI分阶段生成代码任务清单.md`

建议 AI 或开发者严格按照上述文档逐步实现。

---

## 12. 开发建议

推荐开发顺序：
1. 项目初始化
2. 数据库与实体
3. 登录认证
4. 商品与分类
5. 收藏与消息
6. 订单模块
7. 举报与评价
8. 管理后台
9. 优化完善

---

## 13. 后续可扩展方向

- 校园邮箱认证
- WebSocket 即时聊天
- OSS / MinIO 文件存储
- 在线支付
- 更复杂的推荐系统
- 用户信用分体系
- 操作日志与审计系统

---

## 14. 适用场景

本项目适用于：
- Java Web 课程设计
- 毕业设计
- 前后端分离项目实战
- 校园二手平台原型开发

---

## 15. License

本项目仅用于学习、课程设计与技术研究用途。
```

---