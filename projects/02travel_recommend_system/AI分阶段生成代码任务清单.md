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
- 任务名称：初始化 Spring Boot 3 多模块项目骨架（已完成）
- 目标模块：父 POM + 子模块
- 已完成的文件：
  - `pom.xml`（父 POM，`<packaging>pom</packaging>`）
  - `travel-common/pom.xml`（通用组件模块）
  - `travel-model/pom.xml`（数据模型模块）
  - `travel-web/pom.xml`（Web 层模块）
  - `travel-app/pom.xml`（启动模块，含 Spring Boot 打包插件）
  - `travel-app/src/main/resources/application.yml`
  - `travel-app/src/main/resources/application-dev.yml`
  - `travel-app/src/main/resources/application-prod.yml`
  - `travel-app/src/main/java/com/travel/advisor/LLMTravelAdvisorApplication.java`
- 前置依赖：⭕ 已完成
- 跨模块依赖：travel-app → travel-web → travel-common + travel-model
- 关键注意事项：
  - 引入 Spring Web / Validation / MyBatis-Plus / MySQL / Redis / JWT 等依赖
  - 保留 SpringDoc 接口文档能力
  - 单体分层架构，不要微服务化

### 任务 2
- 任务名称：生成基础目录结构与公共模块（已完成）
- 目标模块：
  - Common → `travel-common`（`Result<T>`, `PageQuery`, `PageResult`, `BusinessException`, `GlobalExceptionHandler`）
  - Config 与 Security → `travel-web`（`MybatisPlusConfig`, `RedisConfig`, `ResponseAdviceConfig`）
- 前置依赖：任务1
- 关键注意事项：
  - 统一返回结构放在 travel-common
  - 统一异常处理放在 travel-common
  - 分页对象封装放在 travel-common
  - MyBatis-Plus 分页插件配置放在 travel-web

### 任务 3
- 任务名称：生成基础工具类（已完成）
- 目标模块：travel-common → `com.travel.advisor.utils`
- 前置依赖：任务2
- 关键注意事项：
  - JWT 工具要支持 accessToken 和 refreshToken
  - SecurityUtils 支持获取当前登录用户ID与角色

---

## 阶段二：认证与权限模块

### 任务 4
- 任务名称：生成认证与权限基础代码（已完成）
- 目标模块：
  - Security → `travel-web/src/main/java/com/travel/advisor/security`
  - LoginUser → `travel-common/src/main/java/com/travel/advisor/security/LoginUser.java`
  - Config → `travel-web/src/main/java/com/travel/advisor/config`
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 区分用户端与管理员端登录
  - 支持白名单接口
  - 支持 token 黑名单校验

### 任务 5
- 任务名称：生成用户认证模块代码（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 实现注册、登录、登出、刷新Token、重置密码
  - 用户密码必须加密存储
  - Redis 保存验证码与 token
- 文件清单：
  - `travel-web/controller/user/AuthController.java`
  - `travel-web/service/AuthService.java` + impl
  - `travel-web/dto/auth/UserRegisterDTO.java`
  - `travel-web/dto/auth/UserLoginDTO.java`
  - `travel-web/dto/auth/ResetPasswordDTO.java`
  - `travel-web/vo/auth/LoginVO.java`
  - `travel-model/entity/User.java`
  - `travel-model/mapper/UserMapper.java`

### 任务 6
- 任务名称：生成管理员认证模块代码（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 管理员登录接口独立
  - 管理员 token 与用户 token Redis key 隔离
- 文件清单：
  - `travel-web/controller/admin/AdminAuthController.java`
  - `travel-web/service/AdminAuthService.java` + impl
  - `travel-model/entity/AdminUser.java`
  - `travel-model/mapper/AdminUserMapper.java`
  - `travel-web/dto/auth/AdminLoginDTO.java`

---

## 阶段三：基础数据模块

### 任务 7
- 任务名称：生成地区模块代码（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 同时支持用户端树形查询与管理端 CRUD
- 文件清单：
  - `travel-web/controller/common/RegionController.java`
  - `travel-web/controller/admin/AdminRegionController.java`
  - `travel-web/service/RegionService.java` + impl
  - `travel-model/entity/Region.java`
  - `travel-model/mapper/RegionMapper.java`
  - `travel-web/vo/region/RegionTreeVO.java`

### 任务 8
- 任务名称：生成标签模块代码（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 支持按类型筛选标签
  - 预留用户偏好标签与景点标签共用能力
- 文件清单：
  - `travel-web/controller/common/TagController.java`
  - `travel-web/controller/admin/AdminTagController.java`
  - `travel-web/service/TagService.java` + impl
  - `travel-model/entity/Tag.java`
  - `travel-model/mapper/TagMapper.java`

---

## 阶段四：景点模块

### 任务 9
- 任务名称：生成景点实体与Mapper（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 严格映射现有表
  - 不自行扩展不存在的核心表
- 文件清单：
  - `travel-model/entity/ScenicSpot.java`
  - `travel-model/entity/ScenicImage.java`
  - `travel-model/entity/ScenicSpotTag.java`
  - `travel-model/mapper/ScenicSpotMapper.java`
  - `travel-model/mapper/ScenicImageMapper.java`
  - `travel-model/mapper/ScenicSpotTagMapper.java`

### 任务 10
- 任务名称：生成用户端景点查询模块（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 支持分页、筛选、排序、详情
  - 详情页返回图片、标签、地区、评分、收藏状态
- 文件清单：
  - `travel-web/controller/user/ScenicSpotController.java`
  - `travel-web/service/ScenicSpotService.java` + impl
  - `travel-web/dto/scenic/ScenicQueryDTO.java`
  - `travel-web/vo/scenic/ScenicListVO.java`
  - `travel-web/vo/scenic/ScenicDetailVO.java`
  - `travel-web/vo/scenic/ScenicImageVO.java`

### 任务 11
- 任务名称：生成管理端景点管理模块（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 覆盖 CRUD、上下架、标签绑定、图片关联
- 文件清单：
  - `travel-web/controller/admin/AdminScenicSpotController.java`
  - `travel-web/dto/scenic/ScenicCreateDTO.java`
  - `travel-web/dto/scenic/ScenicUpdateDTO.java`
  - `travel-web/dto/scenic/ScenicStatusDTO.java`
  - `travel-web/dto/scenic/ScenicImageCreateDTO.java`

---

## 阶段五：用户行为模块

### 任务 12
- 任务名称：生成收藏模块代码（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 防止重复收藏
  - 收藏列表支持分页
- 文件清单：
  - `travel-model/entity/UserFavorite.java`
  - `travel-model/mapper/UserFavoriteMapper.java`
  - `travel-web/controller/user/FavoriteController.java`
  - `travel-web/service/FavoriteService.java` + impl
  - `travel-web/vo/favorite/FavoriteVO.java`

### 任务 13
- 任务名称：生成浏览历史模块代码（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 支持记录停留时长和来源
  - 用户只能查看自己的浏览记录
- 文件清单：
  - `travel-model/entity/UserBrowseHistory.java`
  - `travel-model/mapper/UserBrowseHistoryMapper.java`
  - `travel-web/controller/user/BrowseHistoryController.java`
  - `travel-web/service/BrowseHistoryService.java` + impl
  - `travel-web/dto/history/BrowseHistoryCreateDTO.java`
  - `travel-web/vo/history/BrowseHistoryVO.java`

### 任务 14
- 任务名称：生成点评模块代码（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 发布点评后同步生成审核记录
  - 点评状态应受审核结果影响
- 文件清单：
  - `travel-model/entity/UserReview.java`
  - `travel-model/mapper/UserReviewMapper.java`
  - `travel-web/controller/user/ReviewController.java`
  - `travel-web/service/ReviewService.java` + impl
  - `travel-web/dto/review/ReviewCreateDTO.java`
  - `travel-web/vo/review/ReviewVO.java`

---

## 阶段六：审核与用户管理模块

### 任务 15
- 任务名称：生成内容审核模块代码（已完成）
- 前置依赖：⭕ 已完成
- 关键注意事项：
  - 支持通过、拒绝、隐藏
  - 审核结果要回写 user_review 状态
- 文件清单：
  - `travel-model/entity/ContentAudit.java`
  - `travel-model/mapper/ContentAuditMapper.java`
  - `travel-common/common/enums/ContentAuditStatusEnum.java`（审核状态枚举：待审核/通过/拒绝/人工复审）
  - `travel-common/common/enums/UserReviewStatusEnum.java`（点评状态枚举：待审核/通过/拒绝/隐藏）
  - Audit 相关代码已完成（AuditController、AuditService 等）

### 任务 16
- 任务名称：生成管理员用户管理模块代码（已完成）
- 前置依赖：阶段二、阶段五
- 关键注意事项：
  - 支持禁用/启用用户
  - 用户禁用时清理登录态
- 实现说明：
  - 已提供管理端用户分页查询、用户详情查询、用户状态更新接口
  - 用户状态更新为禁用（status=0）时，会清理该用户在 Redis 中的登录态并拉黑现有 tokenId
- 文件清单：
  - `travel-web/controller/admin/AdminUserController.java`
  - `travel-web/service/AdminUserManageService.java`
  - `travel-web/service/impl/AdminUserManageServiceImpl.java`
  - `travel-web/dto/user/UserQueryDTO.java`
  - `travel-web/dto/user/UserStatusDTO.java`
  - `travel-web/vo/user/AdminUserDetailVO.java`
  - `travel-web/security/TokenService.java`（新增按用户维度清理会话能力）
  - `travel-common/utils/RedisUtils.java`（新增 `scanKeys` 查询方法）

---

## 阶段七：行程计划模块

### 任务 17
- 任务名称：生成行程计划基础代码（已完成）
- 前置依赖：阶段四
- 关键注意事项：
  - 按用户隔离数据
  - 明确 dayNo 与 itemType
- 文件清单：
  - `travel-model/entity/TravelPlan.java`
  - `travel-model/entity/TravelPlanItem.java`
  - `travel-model/mapper/TravelPlanMapper.java`
  - `travel-model/mapper/TravelPlanItemMapper.java`
  - `travel-web/controller/user/TravelPlanController.java`
  - `travel-web/service/TravelPlanService.java` + impl
  - `travel-web/dto/plan/TravelPlanCreateDTO.java`
  - `travel-web/dto/plan/TravelPlanItemCreateDTO.java`
  - `travel-web/vo/plan/TravelPlanDetailVO.java`

### 任务 18
- 任务名称：生成 AI辅助行程模块（待实现）
- 前置依赖：任务17、阶段九前置LLM封装
- 关键注意事项：
  - 仅生成草案
  - 支持保存为正式计划
- 文件清单：
  - `travel-web/controller/user/TravelPlanAiController.java`（待生成）
  - `travel-web/dto/plan/TravelPlanAiGenerateDTO.java`（待生成）
  - `travel-web/vo/plan/TravelPlanAiGenerateVO.java`（待生成）
  - `travel-web/service/TravelPlanAiService.java`（待生成）

---

## 阶段八：推荐系统模块

### 任务 19
- 任务名称：生成推荐实体与记录模块（已完成）
- 前置依赖：阶段五
- 关键注意事项：
  - 推荐记录与结果项一对多映射清晰
- 文件清单：
  - `travel-model/entity/RecommendRecord.java`
  - `travel-model/entity/RecommendResultItem.java`
  - `travel-model/mapper/RecommendRecordMapper.java`
  - `travel-model/mapper/RecommendResultItemMapper.java`

### 任务 20
- 任务名称：生成推荐模块骨架代码（已完成）
- 前置依赖：任务19
- 关键注意事项：
  - 先实现可运行的规则推荐，再预留 LLM 精排
- 文件清单：
  - `travel-web/controller/user/RecommendController.java`
  - `travel-web/service/RecommendService.java` + impl
  - `travel-web/service/impl/strategy/RecallStrategy.java`
  - `travel-web/service/impl/strategy/impl/HotRecallStrategy.java`
  - `travel-web/service/impl/strategy/impl/TagRecallStrategy.java`
  - `travel-web/service/impl/strategy/impl/GeoRecallStrategy.java`
  - `travel-web/service/recommend/RecommendRankService.java`
  - `travel-web/service/recommend/RecommendReasonBuilder.java`
  - `travel-web/vo/recommend/RecommendItemVO.java`

### 任务 21
- 任务名称：生成推荐反馈模块代码（已完成）
- 前置依赖：任务20
- 关键注意事项：
  - 覆盖曝光、点击、收藏反馈
  - 回写 recommend_result_item 或扩展统计逻辑
- 文件清单：
  - `travel-web/dto/recommend/RecommendFeedbackDTO.java`
  - `travel-web/service/RecommendFeedbackService.java`

---

## 阶段九：LLM与对话模块

### 任务 22
- 任务名称：生成LLM调用网关基础封装（已完成）
- 前置依赖：阶段一
- 关键注意事项：
  - 做统一网关抽象
  - 便于后续切换模型服务商
- 文件清单：
  - `travel-web/llm/LlmGateway.java`
  - `travel-web/llm/impl/DefaultLlmGateway.java`
  - `travel-web/dto/llm/LlmChatRequest.java`
  - `travel-web/dto/llm/LlmChatResponse.java`
  - `travel-web/llm/PromptBuilder.java`
  - `travel-web/llm/LlmProperties.java`

### 任务 23
- 任务名称：生成对话模块基础代码（已完成）
- 前置依赖：任务22
- 关键注意事项：
  - 支持创建会话、发送消息、查询历史、删除会话
- 文件清单：
  - `travel-model/entity/LlmConversation.java`
  - `travel-model/entity/LlmMessage.java`
  - `travel-model/entity/LlmCallLog.java`
  - `travel-model/mapper/LlmConversationMapper.java`
  - `travel-model/mapper/LlmMessageMapper.java`
  - `travel-model/mapper/LlmCallLogMapper.java`
  - `travel-web/controller/user/ChatController.java`
  - `travel-web/service/ConversationService.java`
  - `travel-web/service/MessageService.java`
  - `travel-web/service/LlmCallLogService.java`

### 任务 24
- 任务名称：生成上下文管理与Prompt注入逻辑（已完成）
- 前置依赖：任务23
- 关键注意事项：
  - 支持当前景点上下文注入
  - 支持消息截断与摘要预留
- 文件清单：
  - `travel-web/llm/ConversationContextManager.java`
  - `travel-web/llm/SensitiveFilterService.java`
  - `travel-web/llm/ChatFallbackService.java`

---

## 阶段十：系统配置、日志、统计模块

### 任务 25
- 任务名称：生成系统配置模块代码（已完成）
- 前置依赖：阶段二
- 关键注意事项：
  - 配置支持按 key 查询与更新
- 文件清单：
  - `travel-model/entity/SystemConfig.java`
  - `travel-model/mapper/SystemConfigMapper.java`
  - `travel-web/controller/admin/SystemConfigController.java`
  - `travel-web/service/SystemConfigService.java`

### 任务 26
- 任务名称：生成操作日志模块代码（已完成）
- 前置依赖：阶段一
- 关键注意事项：
  - 使用 AOP 记录管理员操作
- 文件清单：
  - `travel-model/entity/OperationLog.java`
  - `travel-model/mapper/OperationLogMapper.java`
  - `travel-web/controller/admin/OperationLogController.java`
  - `travel-web/aspect/OperationLogAspect.java`
  - `travel-web/annotation/OperationLog.java`

### 任务 27
- 任务名称：生成统计看板模块代码（已完成）
- 前置依赖：阶段八、阶段九、任务26
- 关键注意事项：
  - 统计接口优先返回图表可直接消费的数据结构
- 文件清单：
  - `travel-model/entity/StatScenicDaily.java`
  - `travel-model/entity/StatPlatformDaily.java`
  - `travel-model/mapper/StatScenicDailyMapper.java`
  - `travel-model/mapper/StatPlatformDailyMapper.java`
  - `travel-web/controller/admin/DashboardController.java`
  - `travel-web/service/DashboardService.java`

---

## 阶段十一：文件资源预留模块

### 任务 28
- 任务名称：生成文件资源模块预留代码（待实现）
- 前置依赖：阶段一
- 关键注意事项：
  - 只做元数据登记与接口预留
  - 不深度实现 MinIO 上传
- 文件清单：
  - `travel-model/entity/FileResource.java`（待生成）
  - `travel-model/mapper/FileResourceMapper.java`（待生成）
  - `travel-web/controller/common/FileController.java`（待生成）
  - `travel-web/service/FileService.java`（待生成）
  - `travel-web/dto/file/FileUploadCallbackDTO.java`（待生成）

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
> 注：本项目已拆分为多模块（travel-common / travel-model / travel-web / travel-app），基础工程已完成。后续生成代码时请将文件放在对应模块中：
> - 实体/ Mapper → `travel-model/src/main/java/com/travel/advisor/entity|mapper/`
> - Service/Controller/DTO/VO → `travel-web/src/main/java/com/travel/advisor/`
> - 通用组件/Result/Page → `travel-common/src/main/java/com/travel/advisor/`
> - 配置文件 → `travel-app/src/main/resources/`

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
- AuditQueryDTO（分页查询参数）
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

### 项目模块结构（2026-04-08 更新）

本项目已拆分为 Maven 多模块结构，生成代码时请将文件放在对应模块中：

| 模块 | 路径前缀 | 放置内容 |
|------|----------|----------|
| travel-common | `travel-common/src/main/java/com/travel/advisor/` | Result, PageQuery, PageResult, 工具类, 异常, LoginUser, 注解, 通用枚举（如 `common/enums/RecommendType`） |
| travel-model | `travel-model/src/main/java/com/travel/advisor/` | Entity, Mapper, XML mapper, 推荐领域模型（如 `domain/recommend/RecallContext`、`RecallCandidate`、`RankedRecommend`） |
| travel-web | `travel-web/src/main/java/com/travel/advisor/` | Controller, Service, DTO, VO, Security, Config, Filter, 推荐业务组件（如 `recommend/RecommendRankService`、`RecommendReasonBuilder`、`service/impl/strategy/**`） |
| travel-app | `travel-app/src/main/` | 主启动类, application*.yml |

跨模块依赖: travel-app → travel-web → travel-common + travel-model

当前推荐模块相关目录（已落地）：

- `travel-common/src/main/java/com/travel/advisor/common/enums/RecommendType.java`
- `travel-model/src/main/java/com/travel/advisor/domain/recommend/`
- `travel-web/src/main/java/com/travel/advisor/recommend/RecommendRankService.java`
- `travel-web/src/main/java/com/travel/advisor/recommend/RecommendReasonBuilder.java`
- `travel-web/src/main/java/com/travel/advisor/service/impl/strategy/RecallStrategy.java`
- `travel-web/src/main/java/com/travel/advisor/service/impl/strategy/impl/`

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
