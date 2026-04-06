# campus-resource-sharing

基于 **Spring Boot 3 + Vue 3** 的校园闲置物资共享平台，面向高校学生场景，提供闲置商品发布、浏览、收藏、咨询、下单、评价、举报及后台管理等功能。

---

## 1. 项目简介

`campus-resource-sharing` 是一个适用于校园内部二手转让场景的前后端分离项目，帮助学生高效处理闲置物品，提升资源利用率。

### 1.1 核心能力
- 普通用户注册登录
- 闲置商品发布与管理
- 商品搜索、筛选、详情展示
- 收藏与留言咨询
- 轻量订单交易流程
- 评价与举报
- 管理员后台管理

### 1.2 项目定位
- 适合课程设计 / 毕业设计 / 全栈实战项目
- 聚焦“校园内部二手流转”场景
- 强调可扩展、易二次开发

---

## 2. 技术栈

### 2.1 前端
- Vue 3
- Vite
- Naive UI
- Pinia
- Vue Router
- Axios
- @vicons

### 2.2 后端
- Spring Boot 3
- MyBatis-Plus
- MySQL
- JWT
- Jakarta Validation

---

## 3. 功能概览

### 3.1 用户端
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

### 3.2 管理端
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

## 4. 项目结构

### 4.1 后端结构
```text
backend
├─ src/main/java/com/campus/resourcesharing
│  ├─ controller        # 控制层
│  ├─ service           # 业务接口
│  ├─ service/impl      # 业务实现
│  ├─ mapper            # MyBatis-Plus 映射层
│  ├─ entity            # 实体类
│  ├─ dto               # 数据传输对象
│  ├─ vo                # 视图对象
│  ├─ query             # 查询对象
│  ├─ config            # 配置类
│  ├─ interceptor       # 拦截器
│  ├─ utils             # 工具类
│  └─ common            # 通用返回/异常等
├─ src/main/resources
│  ├─ application.yml
│  └─ mapper
```

### 4.2 前端结构
```text
frontend
├─ src
│  ├─ api               # 接口请求封装
│  ├─ assets            # 静态资源
│  ├─ components        # 通用组件
│  ├─ layout            # 布局组件
│  ├─ router            # 路由配置
│  ├─ store             # Pinia 状态管理
│  ├─ utils             # 工具方法
│  └─ views             # 页面视图
```

---

## 5. 快速开始

### 5.1 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.x
- Node.js 18+
- npm / pnpm / yarn

### 5.2 数据库初始化
1. 创建 MySQL 数据库（如：`campus_resource_sharing`）
2. 执行 `数据库设计.sql`
3. 修改后端 `application.yml` 中的数据源配置

### 5.3 启动后端
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端默认地址：
```text
http://localhost:8080
```

### 5.4 启动前端
```bash
cd frontend
npm install
npm run dev
```

前端默认地址：
```text
http://localhost:5173
```

---

## 6. 配置说明

### 6.1 后端数据库配置（`application.yml`）
```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_resource_sharing?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### 6.2 前端环境变量（可选）
可在 `frontend/.env.development` 中配置：
```env
VITE_API_BASE_URL=http://localhost:8080
```

---

## 7. 默认账号说明

### 管理员账号
- 用户名：`admin`
- 密码：请在后端初始化后自行重置或替换为加密后的安全密码

---

## 8. 项目特色

- 面向校园场景设计
- 采用轻量级交易流程
- 不依赖在线支付
- 前后端分离架构清晰
- 具备良好的扩展性与二开能力

---

## 9. 页面设计风格

- 主题色：`#ffe60f`
- 文字主色：黑色
- 用户端：简洁、清新、校园风
- 管理端：规范、清晰、易操作

---

## 10. 配套文档

项目配套文档包括：
- `开发文档.md`
- `数据库设计.sql`
- `后端接口规范文档.md`
- `前端页面与路由设计.md`
- `AI分阶段生成代码任务清单.md`

建议 AI 或开发者严格按照上述文档逐步实现。

---

## 11. 开发建议

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

## 12. 后续可扩展方向

- 校园邮箱认证
- WebSocket 即时聊天
- OSS / MinIO 文件存储
- 在线支付
- 更复杂的推荐系统
- 用户信用分体系
- 操作日志与审计系统

---

## 13. 适用场景

本项目适用于：
- Java Web 课程设计
- 毕业设计
- 前后端分离项目实战
- 校园二手平台原型开发

---

## 14. License

本项目仅用于学习、课程设计与技术研究用途。
