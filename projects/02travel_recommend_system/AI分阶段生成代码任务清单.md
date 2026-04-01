# AI分阶段生成代码任务清单

## 1. 文档目标
本清单用于指导代码生成型 AI 按阶段、按模块、按依赖顺序逐步生成 `LLM-Travel-Advisor` 项目的前后端代码，确保：
- 可持续开发
- 依赖清晰
- 模块边界明确
- 便于逐轮验收

---

## 2. 总体开发顺序

推荐开发顺序如下：

1. 基础工程搭建
2. 通用基础模块
3. 安全认证模块
4. 基础数据模块（地区、标签）
5. 景点模块
6. 收藏、浏览历史、点评模块
7. 审核模块
8. 行程计划模块
9. 推荐模块
10. AI对话模块
11. 管理后台模块
12. 统计、看板、日志模块
13. 文件资源预留模块
14. 前端联调与页面完善

---

## 3. 分阶段任务列表

## 阶段一：后端基础工程搭建

### 任务 1
- 任务名称：初始化 Spring Boot 3 项目骨架
- 目标模块：基础工程
- 要生成的文件：
  - `pom.xml`
  - `application.yml`
  - `application-dev.yml`
  - `application-prod.yml`
  - `LLMTravelAdvisorApplication.java`
- 前置依赖：无
- 关键注意事项：
  - 引入 Spring Web / Validation / MyBatis-Plus / MySQL / Redis / JWT 等依赖
  - 保留 Knife4j 或 SpringDoc 接口文档能力
  - 单体分层架构，不要微服务化

### 任务 2
- 任务名称：生成基础目录结构与公共模块
- 目标模块：common/config/exception
- 要生成的文件：
  - `common/result/Result.java`
  - `common/result/ResultCode.java`
  - `common/page/PageQuery.java`
  - `common/page/PageResult.java`
  - `exception/BusinessException.java`
  - `exception/GlobalExceptionHandler.java`
  - `config/MybatisPlusConfig.java`
  - `config/RedisConfig.java`
- 前置依赖：任务1
- 关键注意事项：
  - 统一返回结构
  - 统一异常处理
  - 分页对象封装
  - MyBatis-Plus 分页插件配置

### 任务 3
- 任务名称：生成基础工具类
- 目标模块：utils
- 要生成的文件：
  - `utils/JwtUtils.java`
  - `utils/RedisUtils.java`
  - `utils/SecurityUtils.java`
  - `utils/BeanCopyUtils.java`
  - `utils/JsonUtils.java`
- 前置依赖：任务2
- 关键注意事项：
  - JWT 工具要支持 accessToken 和 refreshToken
  - SecurityUtils 支持获取当前登录用户ID与角色

---

## 阶段二：认证与权限模块

### 任务 4
- 任务名称：生成认证与权限基础代码
- 目标模块：security/auth
- 要生成的文件：
  - `security/LoginUser.java`
  - `security/JwtAuthenticationFilter.java`
  - `security/SecurityConfig.java`
  - `security/TokenService.java`
  - `security/PermissionService.java`
- 前置依赖：阶段一
- 关键注意事项：
  - 区分用户端与管理员端登录
  - 支持白名单接口
  - 支持 token 黑名单校验

### 任务 5
- 任务名称：生成用户认证模块代码
- 目标模块：user auth
- 要生成的文件：
  - `controller/user/AuthController.java`
  - `service/AuthService.java`
  - `service/impl/AuthServiceImpl.java`
  - `dto/auth/UserRegisterDTO.java`
  - `dto/auth/UserLoginDTO.java`
  - `dto/auth/ResetPasswordDTO.java`
  - `vo/auth/LoginVO.java`
  - `entity/User.java`
  - `mapper/UserMapper.java`
- 前置依赖：任务4
- 关键注意事项：
  - 实现注册、登录、登出、刷新Token、重置密码
  - 用户密码必须加密存储
  - Redis 保存验证码与 token

### 任务 6
- 任务名称：生成管理员认证模块代码
- 目标模块：admin auth
- 要生成的文件：
  - `controller/admin/AdminAuthController.java`
  - `service/AdminAuthService.java`
  - `service/impl/AdminAuthServiceImpl.java`
  - `entity/AdminUser.java`
  - `mapper/AdminUserMapper.java`
  - `dto/auth/AdminLoginDTO.java`
- 前置依赖：任务4
- 关键注意事项：
  - 管理员登录接口独立
  - 管理员 token 与用户 token Redis key 隔离

---

## 阶段三：基础数据模块

### 任务 7
- 任务名称：生成地区模块代码
- 目标模块：region
- 要生成的文件：
  - `controller/common/RegionController.java`
  - `controller/admin/AdminRegionController.java`
  - `service/RegionService.java`
  - `service/impl/RegionServiceImpl.java`
  - `entity/Region.java`
  - `mapper/RegionMapper.java`
  - `vo/region/RegionTreeVO.java`
- 前置依赖：阶段二
- 关键注意事项：
  - 同时支持用户端树形查询与管理端 CRUD

### 任务 8
- 任务名称：生成标签模块代码
- 目标模块：tag
- 要生成的文件：
  - `controller/common/TagController.java`
  - `controller/admin/AdminTagController.java`
  - `service/TagService.java`
  - `service/impl/TagServiceImpl.java`
  - `entity/Tag.java`
  - `mapper/TagMapper.java`
- 前置依赖：阶段二
- 关键注意事项：
  - 支持按类型筛选标签
  - 预留用户偏好标签与景点标签共用能力

---

## 阶段四：景点模块

### 任务 9
- 任务名称：生成景点实体与Mapper
- 目标模块：scenic
- 要生成的文件：
  - `entity/ScenicSpot.java`
  - `entity/ScenicImage.java`
  - `entity/ScenicSpotTag.java`
  - `mapper/ScenicSpotMapper.java`
  - `mapper/ScenicImageMapper.java`
  - `mapper/ScenicSpotTagMapper.java`
- 前置依赖：阶段三
- 关键注意事项：
  - 严格映射现有表
  - 不自行扩展不存在的核心表

### 任务 10
- 任务名称：生成用户端景点查询模块
- 目标模块：user scenic
- 要生成的文件：
  - `controller/user/ScenicSpotController.java`
  - `service/ScenicSpotService.java`
  - `service/impl/ScenicSpotServiceImpl.java`
  - `dto/scenic/ScenicQueryDTO.java`
  - `vo/scenic/ScenicListVO.java`
  - `vo/scenic/ScenicDetailVO.java`
- 前置依赖：任务9
- 关键注意事项：
  - 支持分页、筛选、排序、详情
  - 详情页返回图片、标签、地区、评分、收藏状态

### 任务 11
- 任务名称：生成管理端景点管理模块
- 目标模块：admin scenic
- 要生成的文件：
  - `controller/admin/AdminScenicSpotController.java`
  - `dto/scenic/ScenicCreateDTO.java`
  - `dto/scenic/ScenicUpdateDTO.java`
  - `dto/scenic/ScenicStatusDTO.java`
- 前置依赖：任务9、任务10
- 关键注意事项：
  - 覆盖 CRUD、上下架、标签绑定、图片关联

---

## 阶段五：用户行为模块

### 任务 12
- 任务名称：生成收藏模块代码
- 目标模块：favorite
- 要生成的文件：
  - `entity/UserFavorite.java`
  - `mapper/UserFavoriteMapper.java`
  - `controller/user/FavoriteController.java`
  - `service/FavoriteService.java`
  - `service/impl/FavoriteServiceImpl.java`
- 前置依赖：阶段四
- 关键注意事项：
  - 防止重复收藏
  - 收藏列表支持分页

### 任务 13
- 任务名称：生成浏览历史模块代码
- 目标模块：browse history
- 要生成的文件：
  - `entity/UserBrowseHistory.java`
  - `mapper/UserBrowseHistoryMapper.java`
  - `controller/user/BrowseHistoryController.java`
  - `service/BrowseHistoryService.java`
  - `service/impl/BrowseHistoryServiceImpl.java`
  - `dto/history/BrowseHistoryCreateDTO.java`
- 前置依赖：阶段四
- 关键注意事项：
  - 支持记录停留时长和来源
  - 用户只能查看自己的浏览记录

### 任务 14
- 任务名称：生成点评模块代码
- 目标模块：review
- 要生成的文件：
  - `entity/UserReview.java`
  - `mapper/UserReviewMapper.java`
  - `controller/user/ReviewController.java`
  - `service/ReviewService.java`
  - `service/impl/ReviewServiceImpl.java`
  - `dto/review/ReviewCreateDTO.java`
  - `vo/review/ReviewVO.java`
- 前置依赖：阶段四
- 关键注意事项：
  - 发布点评后同步生成审核记录
  - 点评状态应受审核结果影响

---

## 阶段六：审核与用户管理模块

### 任务 15
- 任务名称：生成内容审核模块代码
- 目标模块：audit
- 要生成的文件：
  - `entity/ContentAudit.java`
  - `mapper/ContentAuditMapper.java`
  - `controller/admin/AuditController.java`
  - `service/AuditService.java`
  - `service/impl/AuditServiceImpl.java`
  - `dto/audit/AuditActionDTO.java`
- 前置依赖：任务14
- 关键注意事项：
  - 支持通过、拒绝、隐藏
  - 审核结果要回写 user_review 状态

### 任务 16
- 任务名称：生成管理员用户管理模块代码
- 目标模块：admin user
- 要生成的文件：
  - `controller/admin/AdminUserController.java`
  - `service/AdminUserManageService.java`
  - `service/impl/AdminUserManageServiceImpl.java`
  - `dto/user/UserStatusDTO.java`
  - `vo/user/AdminUserDetailVO.java`
- 前置依赖：阶段二、阶段五
- 关键注意事项：
  - 支持禁用/启用用户
  - 用户禁用时清理登录态

---

## 阶段七：行程计划模块

### 任务 17
- 任务名称：生成行程计划基础代码
- 目标模块：travel plan
- 要生成的文件：
  - `entity/TravelPlan.java`
  - `entity/TravelPlanItem.java`
  - `mapper/TravelPlanMapper.java`
  - `mapper/TravelPlanItemMapper.java`
  - `controller/user/TravelPlanController.java`
  - `service/TravelPlanService.java`
  - `service/impl/TravelPlanServiceImpl.java`
  - `dto/plan/TravelPlanCreateDTO.java`
  - `dto/plan/TravelPlanItemCreateDTO.java`
  - `vo/plan/TravelPlanDetailVO.java`
- 前置依赖：阶段四
- 关键注意事项：
  - 按用户隔离数据
  - 明确 dayNo 与 itemType

### 任务 18
- 任务名称：生成 AI辅助行程模块
- 目标模块：travel plan ai
- 要生成的文件：
  - `controller/user/TravelPlanAiController.java`
  - `dto/plan/TravelPlanAiGenerateDTO.java`
  - `vo/plan/TravelPlanAiGenerateVO.java`
  - `service/TravelPlanAiService.java`
  - `service/impl/TravelPlanAiServiceImpl.java`
- 前置依赖：任务17、阶段九前置LLM封装
- 关键注意事项：
  - 仅生成草案
  - 支持保存为正式计划

---

## 阶段八：推荐系统模块

### 任务 19
- 任务名称：生成推荐实体与记录模块
- 目标模块：recommend record
- 要生成的文件：
  - `entity/RecommendRecord.java`
  - `entity/RecommendResultItem.java`
  - `mapper/RecommendRecordMapper.java`
  - `mapper/RecommendResultItemMapper.java`
- 前置依赖：阶段五
- 关键注意事项：
  - 推荐记录与结果项一对多映射清晰

### 任务 20
- 任务名称：生成推荐模块骨架代码
- 目标模块：recommend
- 要生成的文件：
  - `controller/user/RecommendController.java`
  - `service/RecommendService.java`
  - `service/impl/RecommendServiceImpl.java`
  - `recommend/RecallStrategy.java`
  - `recommend/HotRecallStrategy.java`
  - `recommend/TagRecallStrategy.java`
  - `recommend/GeoRecallStrategy.java`
  - `recommend/RecommendRankService.java`
  - `recommend/RecommendReasonBuilder.java`
  - `vo/recommend/RecommendItemVO.java`
- 前置依赖：任务19
- 关键注意事项：
  - 先实现可运行的规则推荐，再预留 LLM 精排

### 任务 21
- 任务名称：生成推荐反馈模块代码
- 目标模块：recommend feedback
- 要生成的文件：
  - `dto/recommend/RecommendFeedbackDTO.java`
  - `service/RecommendFeedbackService.java`
  - `service/impl/RecommendFeedbackServiceImpl.java`
- 前置依赖：任务20
- 关键注意事项：
  - 覆盖曝光、点击、收藏反馈
  - 回写 recommend_result_item 或扩展统计逻辑

---

## 阶段九：LLM与对话模块

### 任务 22
- 任务名称：生成LLM调用网关基础封装
- 目标模块：llm gateway
- 要生成的文件：
  - `llm/LlmGateway.java`
  - `llm/impl/DefaultLlmGateway.java`
  - `llm/dto/LlmChatRequest.java`
  - `llm/dto/LlmChatResponse.java`
  - `llm/PromptBuilder.java`
  - `llm/LlmProperties.java`
- 前置依赖：阶段一
- 关键注意事项：
  - 做统一网关抽象
  - 便于后续切换模型服务商

### 任务 23
- 任务名称：生成对话模块基础代码
- 目标模块：chat
- 要生成的文件：
  - `entity/LlmConversation.java`
  - `entity/LlmMessage.java`
  - `entity/LlmCallLog.java`
  - `mapper/LlmConversationMapper.java`
  - `mapper/LlmMessageMapper.java`
  - `mapper/LlmCallLogMapper.java`
  - `controller/user/ChatController.java`
  - `service/ConversationService.java`
  - `service/MessageService.java`
  - `service/LlmCallLogService.java`
- 前置依赖：任务22
- 关键注意事项：
  - 支持创建会话、发送消息、查询历史、删除会话

### 任务 24
- 任务名称：生成上下文管理与Prompt注入逻辑
- 目标模块：chat context
- 要生成的文件：
  - `llm/ConversationContextManager.java`
  - `llm/SensitiveFilterService.java`
  - `llm/ChatFallbackService.java`
- 前置依赖：任务23
- 关键注意事项：
  - 支持当前景点上下文注入
  - 支持消息截断与摘要预留

---

## 阶段十：系统配置、日志、统计模块

### 任务 25
- 任务名称：生成系统配置模块代码
- 目标模块：system config
- 要生成的文件：
  - `entity/SystemConfig.java`
  - `mapper/SystemConfigMapper.java`
  - `controller/admin/SystemConfigController.java`
  - `service/SystemConfigService.java`
  - `service/impl/SystemConfigServiceImpl.java`
- 前置依赖：阶段二
- 关键注意事项：
  - 配置支持按 key 查询与更新

### 任务 26
- 任务名称：生成操作日志模块代码
- 目标模块：operation log
- 要生成的文件：
  - `entity/OperationLog.java`
  - `mapper/OperationLogMapper.java`
  - `controller/admin/OperationLogController.java`
  - `aspect/OperationLogAspect.java`
  - `annotation/OperationLog.java`
- 前置依赖：阶段一
- 关键注意事项：
  - 使用 AOP 记录管理员操作

### 任务 27
- 任务名称：生成统计看板模块代码
- 目标模块：dashboard
- 要生成的文件：
  - `entity/StatScenicDaily.java`
  - `entity/StatPlatformDaily.java`
  - `mapper/StatScenicDailyMapper.java`
  - `mapper/StatPlatformDailyMapper.java`
  - `controller/admin/DashboardController.java`
  - `service/DashboardService.java`
  - `service/impl/DashboardServiceImpl.java`
- 前置依赖：阶段八、阶段九、任务26
- 关键注意事项：
  - 统计接口优先返回图表可直接消费的数据结构

---

## 阶段十一：文件资源预留模块

### 任务 28
- 任务名称：生成文件资源模块预留代码
- 目标模块：file resource
- 要生成的文件：
  - `entity/FileResource.java`
  - `mapper/FileResourceMapper.java`
  - `controller/common/FileController.java`
  - `service/FileService.java`
  - `service/impl/FileServiceImpl.java`
  - `dto/file/FileUploadCallbackDTO.java`
- 前置依赖：阶段一
- 关键注意事项：
  - 只做元数据登记与接口预留
  - 不深度实现 MinIO 上传

---

## 阶段十二：前端基础与页面开发

### 任务 29
- 任务名称：生成 Vue3 前端基础工程结构
- 目标模块：frontend base
- 要生成的文件：
  - `main.ts`
  - `App.vue`
  - `router/index.ts`
  - `router/guard.ts`
  - `store/index.ts`
  - `utils/request.ts`
  - `layouts/UserLayout.vue`
  - `layouts/AdminLayout.vue`
- 前置依赖：接口文档完成
- 关键注意事项：
  - 区分用户端和管理端布局
  - 封装请求拦截器和 token 注入

### 任务 30
- 任务名称：生成登录与认证页面
- 目标模块：frontend auth
- 要生成的文件：
  - `views/auth/login.vue`
  - `views/auth/register.vue`
  - `views/auth/reset-password.vue`
  - `views/auth/admin-login.vue`
  - `api/auth.ts`
  - `store/modules/auth.ts`
- 前置依赖：任务29
- 关键注意事项：
  - 用户端与管理端登录页分离

### 任务 31
- 任务名称：生成景点相关页面
- 目标模块：frontend scenic
- 要生成的文件：
  - `views/user/home/index.vue`
  - `views/user/scenic/list.vue`
  - `views/user/scenic/detail.vue`
  - `components/scenic/ScenicCard.vue`
  - `components/scenic/ScenicFilterPanel.vue`
- 前置依赖：任务29
- 关键注意事项：
  - 首页和详情页风格需符合旅游平台展示

### 任务 32
- 任务名称：生成用户行为页面
- 目标模块：frontend user action
- 要生成的文件：
  - `views/user/favorites/index.vue`
  - `views/user/history/index.vue`
  - `views/user/review/my.vue`
- 前置依赖：任务31
- 关键注意事项：
  - 与对应 API 模块联动

### 任务 33
- 任务名称：生成 AI 推荐与聊天页面
- 目标模块：frontend ai
- 要生成的文件：
  - `views/user/recommend/index.vue`
  - `views/user/chat/index.vue`
  - `views/user/chat/detail.vue`
  - `components/recommend/RecommendCard.vue`
  - `components/recommend/RecommendFeed.vue`
  - `components/recommend/RecommendReasonTag.vue`
  - `components/chat/ChatMessageList.vue`
  - `components/chat/ChatBubble.vue`
  - `components/chat/ChatInputBox.vue`
  - `components/chat/ChatSceneTips.vue`
  - `components/chat/ScenicContextCard.vue`
  - `api/recommend.ts`
  - `api/chat.ts`
  - `store/modules/recommend.ts`
  - `store/modules/chat.ts`
  - `hooks/useRecommend.ts`
  - `hooks/useChat.ts`
- 前置依赖：任务29、任务31，以及后端推荐接口与对话接口基础可用
- 关键注意事项：
  - 推荐页要支持推荐理由展示、曝光上报、点击上报、收藏反馈
  - 聊天页要支持会话列表、消息列表、发送消息、删除会话
  - 聊天详情页需支持“当前景点上下文注入”入口
  - 页面要处理 AI 回复中的加载状态、失败重试、空状态
  - 推荐卡片要兼顾旅游图片展示与理由文本可读性

### 任务 34
- 任务名称：生成行程计划页面
- 目标模块：frontend travel plan
- 要生成的文件：
  - `views/user/travel-plan/list.vue`
  - `views/user/travel-plan/create.vue`
  - `views/user/travel-plan/detail.vue`
  - `views/user/travel-plan/ai-generate.vue`
  - `components/travel/TravelPlanCard.vue`
  - `components/travel/TravelPlanDayCard.vue`
  - `components/travel/TravelPlanItemEditor.vue`
  - `components/travel/AiPlanPreview.vue`
  - `api/travelPlan.ts`
- 前置依赖：任务29、任务31，以及后端行程模块基础接口可用
- 关键注意事项：
  - 手动创建与 AI 生成要分页面处理
  - AI生成页面需支持“生成草案 -> 预览 -> 保存正式计划”
  - 行程详情页应按 dayNo 分组展示 travel_plan_item

### 任务 35
- 任务名称：生成用户中心页面
- 目标模块：frontend profile
- 要生成的文件：
  - `views/user/profile/index.vue`
  - `components/common/UserAvatarUpload.vue`
  - `components/common/TagMultiSelect.vue`
  - `api/profile.ts`
  - `store/modules/user.ts`
- 前置依赖：任务29、任务30
- 关键注意事项：
  - 展示基本资料、偏好标签、用户画像摘要
  - 头像上传仅做前端接口预留，不要求完整文件系统实现

### 任务 36
- 任务名称：生成管理后台基础框架页面
- 目标模块：frontend admin base
- 要生成的文件：
  - `views/admin/dashboard/index.vue`
  - `components/admin/AdminSideMenu.vue`
  - `components/admin/AdminHeader.vue`
  - `components/admin/DashboardCard.vue`
  - `components/admin/ChartPanel.vue`
  - `api/admin/dashboard.ts`
  - `store/modules/admin.ts`
- 前置依赖：任务29、任务30
- 关键注意事项：
  - 完成后台整体布局、菜单、面包屑、权限控制基础能力
  - 图表使用 ECharts，接口响应格式要适配图表组件

### 任务 37
- 任务名称：生成管理后台业务页面
- 目标模块：frontend admin business
- 要生成的文件：
  - `views/admin/scenic/list.vue`
  - `views/admin/scenic/create.vue`
  - `views/admin/scenic/edit.vue`
  - `views/admin/audit/reviews.vue`
  - `views/admin/user/list.vue`
  - `views/admin/region/index.vue`
  - `views/admin/tag/index.vue`
  - `views/admin/system-config/index.vue`
  - `views/admin/log/index.vue`
  - `views/admin/analytics/recommend.vue`
  - `views/admin/analytics/llm.vue`
  - `api/admin/scenic.ts`
  - `api/admin/audit.ts`
  - `api/admin/user.ts`
  - `api/admin/region.ts`
  - `api/admin/tag.ts`
  - `api/admin/systemConfig.ts`
  - `api/admin/log.ts`
- 前置依赖：任务36，以及对应后端管理接口基础可用
- 关键注意事项：
  - 列表页统一采用“筛选栏 + 表格 + 分页 + 弹窗/抽屉”的交互模式
  - 景点编辑页需包含标签选择、图片资源引用预留
  - 评论审核页需支持通过、拒绝、隐藏快捷操作
  - 推荐分析与 LLM 分析页优先使用图表卡片组合展示

---

## 4. 联调与优化阶段任务

### 任务 38
- 任务名称：前后端接口联调与统一字段修正
- 目标模块：integration
- 要生成的文件：
  - 接口联调记录文档
  - 字段映射修订说明
  - 错误码对齐清单
- 前置依赖：阶段一到阶段十二基础完成
- 关键注意事项：
  - 核对 DTO、VO、前端表单字段命名一致性
  - 核对分页对象、时间字段、状态枚举返回格式
  - 修复前端筛选项与后端查询参数不一致问题

### 任务 39
- 任务名称：补充统一枚举、常量与校验规则
- 目标模块：refactor
- 要生成的文件：
  - `common/enums/*`
  - `constants/*`
  - 参数校验注解与校验规则说明
- 前置依赖：联调完成
- 关键注意事项：
  - 统一状态码、审核状态、景点状态、对话场景、推荐来源等枚举
  - 前后端状态值保持一致

### 任务 40
- 任务名称：补充测试数据与演示数据初始化脚本
- 目标模块：demo data
- 要生成的文件：
  - `sql/mock_data.sql`
  - `sql/init_config.sql`
  - 测试账号说明文档
- 前置依赖：主要功能模块完成
- 关键注意事项：
  - 包含用户、管理员、景点、标签、地区、点评、推荐测试数据
  - 数据要能支撑页面演示与答辩展示

---

## 5. 推荐的逐轮代码生成策略

### 第一轮
先生成：
- Spring Boot 基础工程
- 通用返回结构
- 全局异常
- MyBatis-Plus 配置
- Redis 配置
- JWT 工具类

### 第二轮
再生成：
- 认证与权限模块
- 用户登录/注册
- 管理员登录
- Token 刷新与黑名单机制

### 第三轮
再生成：
- 地区与标签模块
- 用户资料模块
- 用户偏好标签模块

### 第四轮
再生成：
- 景点模块实体、Mapper、Service、Controller
- 用户端景点列表与详情
- 管理端景点 CRUD

### 第五轮
再生成：
- 收藏、浏览历史、点评模块
- 点评与审核联动

### 第六轮
再生成：
- 行程计划模块
- AI辅助生成行程骨架

### 第七轮
再生成：
- 推荐模块骨架
- 多路召回策略
- 推荐反馈接口
- Redis 缓存逻辑

### 第八轮
再生成：
- LLM 调用网关
- 对话模块
- 上下文管理
- Prompt 注入逻辑

### 第九轮
再生成：
- 管理后台用户管理、配置管理、日志管理
- 运营看板统计接口

### 第十轮
再生成：
- 前端用户端页面
- 前端管理端页面
- 前后端联调收尾

---

## 6. 下一轮可直接复制使用的代码生成 Prompt 模板

### 模板 1：生成后端基础工程
```text
请为项目 LLM-Travel-Advisor 生成 Spring Boot 3 后端基础工程骨架，技术栈为 Spring Boot 3 + MyBatis-Plus + MySQL + Redis + JWT。请输出：
1. pom.xml 依赖
2. application.yml 基础配置
3. 主启动类
4. common/result 包下统一响应结构 Result、ResultCode
5. common/page 包下分页对象 PageQuery、PageResult
6. 全局异常处理 GlobalExceptionHandler
7. MybatisPlusConfig 与 RedisConfig
要求：
- 代码结构清晰
- 包名规范
- 可直接复制到项目中使用
- 不要省略 import
```

### 模板 2：生成认证模块完整代码
```text
请为 LLM-Travel-Advisor 项目生成“用户认证模块”完整代码，基于 Spring Boot 3 + MyBatis-Plus + Redis + JWT，实现：
- 用户注册
- 用户登录
- 刷新Token
- 用户登出
- 重置密码
请输出以下文件：
- AuthController
- AuthService / AuthServiceImpl
- User 实体
- UserMapper
- UserRegisterDTO
- UserLoginDTO
- ResetPasswordDTO
- LoginVO
- JwtUtils / TokenService 如需补充可一并输出
要求：
- 使用统一响应 Result<T>
- 密码加密存储
- Redis 保存验证码、Token、黑名单
- 接口路径使用 /api/user/auth/*
- 给出必要注释
```

### 模板 3：生成管理员登录与权限模块

```text
请为 LLM-Travel-Advisor 生成管理员登录与权限控制基础代码，实现：
- 管理员登录
- 管理员登出
- JWT 认证过滤器
- Spring Security 配置
- LoginUser 登录上下文
- 区分用户端与管理员端权限
请输出：
- AdminAuthController
- AdminAuthService / Impl
- AdminUser 实体与 Mapper
- SecurityConfig
- JwtAuthenticationFilter
- PermissionService
要求：
- 管理员接口前缀为 /api/admin/*
- 支持 token 黑名单
- 预留 RBAC 扩展能力
```

### 模板 4：生成景点模块完整 CRUD

```text
请为 LLM-Travel-Advisor 生成“景点模块”完整代码，基于 scenic_spot、scenic_image、scenic_spot_tag、tag、region 表，实现：
用户端：
- 景点分页查询
- 景点详情查询
管理端：
- 景点新增
- 景点修改
- 景点删除
- 景点上下架
- 标签绑定
请输出：
- ScenicSpotController
- AdminScenicSpotController
- ScenicSpotService / Impl
- ScenicSpotMapper
- ScenicSpot、ScenicImage、ScenicSpotTag 实体
- ScenicQueryDTO、ScenicCreateDTO、ScenicUpdateDTO
- ScenicListVO、ScenicDetailVO
要求：
- 使用 MyBatis-Plus
- 接口符合 RESTful
- 详情页返回图片、标签、地区信息
```

### 模板 5：生成收藏、浏览历史、点评模块

```text
请为 LLM-Travel-Advisor 生成用户行为模块代码，实现：
- 收藏景点 / 取消收藏 / 收藏列表
- 浏览历史上报 / 查询 / 删除 / 清空
- 发布点评 / 我的点评 / 景点评价列表 / 删除点评
请输出：
- FavoriteController / Service / Mapper / Entity
- BrowseHistoryController / Service / Mapper / Entity
- ReviewController / Service / Mapper / Entity
- 对应 DTO / VO
要求：
- 用户数据严格按 userId 隔离
- 点评发布后要预留审核流入口
- 接口路径使用 /api/user/*
```

### 模板 6：生成点评审核模块

```text
请为 LLM-Travel-Advisor 生成“点评审核模块”代码，基于 content_audit 与 user_review 表，实现：
- 审核列表查询
- 审核通过
- 审核拒绝
- 审核隐藏
并在审核后回写点评状态。
请输出：
- AuditController
- AuditService / AuditServiceImpl
- ContentAudit 实体与 Mapper
- AuditActionDTO
要求：
- 管理端接口前缀 /api/admin/audits
- 给出审核状态枚举建议
- 给出回写 user_review 状态的逻辑
```

### 模板 7：生成行程计划模块

```text
请为 LLM-Travel-Advisor 生成“行程计划模块”代码，基于 travel_plan 与 travel_plan_item 表，实现：
- 创建行程
- 行程列表
- 行程详情
- 修改行程
- 删除行程
- 新增行程项
- 删除行程项
请输出：
- TravelPlanController
- TravelPlanService / Impl
- TravelPlan、TravelPlanItem 实体
- Mapper
- TravelPlanCreateDTO
- TravelPlanItemCreateDTO
- TravelPlanDetailVO
要求：
- 数据按用户隔离
- dayNo 字段用于按天分组展示
- 接口路径使用 /api/user/travel-plans
```

### 模板 8：生成推荐模块骨架代码

```
请为 LLM-Travel-Advisor 生成“推荐模块骨架代码”，实现：
- 首页个性化推荐
- 景点相似推荐
- 推荐曝光反馈
- 推荐点击反馈
- 推荐收藏反馈
请输出：
- RecommendController
- RecommendService / Impl
- RecommendRecord、RecommendResultItem 实体与 Mapper
- RecallStrategy 接口
- HotRecallStrategy
- TagRecallStrategy
- GeoRecallStrategy
- RecommendRankService
- RecommendReasonBuilder
- RecommendFeedbackService
要求：
- 先采用规则召回 + 预留 LLM 精排扩展点
- Redis 缓存推荐结果
- 接口路径使用 /api/user/recommend/*
```

### 模板 9：生成 AI 对话模块骨架代码

```text
请为 LLM-Travel-Advisor 生成“AI对话助手模块”骨架代码，基于 llm_conversation、llm_message、llm_call_log 表，实现：
- 创建会话
- 获取会话列表
- 获取消息列表
- 发送消息
- 删除会话
请输出：
- ChatController
- ConversationService
- MessageService
- LlmCallLogService
- LlmConversation、LlmMessage、LlmCallLog 实体与 Mapper
- LlmGateway
- PromptBuilder
- ConversationContextManager
要求：
- 支持多轮对话
- 支持注入当前浏览景点上下文
- 记录 LLM 调用日志
- 接口路径使用 /api/user/chat/*
```

### 模板 10：生成 Vue3 前端页面骨架

```text
请为 LLM-Travel-Advisor 生成 Vue3 + Vite + Naive UI 前端页面骨架，包含：
用户端：
- 首页
- 景点列表页
- 景点详情页
- 收藏页
- AI推荐页
- AI聊天页
- 行程列表页
- 个人中心页
管理端：
- 后台登录页
- 工作台
- 景点管理页
- 评论审核页
- 用户管理页
请输出：
- router 配置
- layouts 布局
- views 页面骨架
- api 请求模块
- store 模块基础结构
要求：
- 用户端与管理端路由分离
- 页面使用 Naive UI
- 代码风格统一
- 可直接继续扩展联调
```



## 特别说明

### 关于 MinIO 文件模块

本阶段只保留：

- `FileResource` 实体
- `FileController`
- `FileService`
- 上传凭证/回调登记/元数据查询接口边界

不要求：

- 完整对象存储接入
- 分片上传
- 图片处理流水线

###  关于推荐系统

建议分两步实现：

1. 第一版：热门 + 标签 + 地理位置规则召回，支持推荐理由模板化生成
2. 第二版：接入 LLM 做精排与推荐理由自然语言生成

###  关于对话系统

建议分两步实现：

1. 第一版：基础会话、消息存储、LLM网关封装
2. 第二版：上下文摘要、景点上下文注入、成本控制、敏感过滤
