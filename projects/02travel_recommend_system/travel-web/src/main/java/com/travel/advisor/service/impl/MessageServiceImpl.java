package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.enums.ConversationType;
import com.travel.advisor.common.enums.LLMCallLogStatus;
import com.travel.advisor.common.enums.MessageContentType;
import com.travel.advisor.common.enums.MessageRole;
import com.travel.advisor.common.enums.SensitiveStatus;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.chat.ChatSendMessageDTO;
import com.travel.advisor.dto.llm.LlmRequest;
import com.travel.advisor.dto.llm.LlmResponse;
import com.travel.advisor.entity.LlmConversation;
import com.travel.advisor.entity.LlmMessage;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.llm.*;
import com.travel.advisor.mapper.LlmConversationMapper;
import com.travel.advisor.mapper.LlmMessageMapper;
import com.travel.advisor.service.ConversationService;
import com.travel.advisor.service.LlmCallLogService;
import com.travel.advisor.service.MessageService;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.chat.ChatMessageVO;
import com.travel.advisor.vo.chat.ChatSendMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * 消息服务实现类，处理消息查询和发送业务。
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    /**
     * 历史消息最多拉取条数
     */
    private static final int HISTORY_FETCH_LIMIT = 100;
    /**
     * 发送给 LLM 的历史消息 token 上限
     */
    private static final int HISTORY_MAX_TOKENS = 2800;
    /**
     * 自动生成标题最大长度
     */
    private static final int AUTO_TITLE_MAX_LENGTH = 16;
    /**
     * 需要自动覆盖的默认标题
     */
    private static final Set<String> DEFAULT_CONVERSATION_TITLES = Set.of("新的旅行咨询", "新对话", "新的对话", "默认会话");

    private final TransactionTemplate transactionTemplate;
    private final LlmMessageMapper llmMessageMapper;
    private final LlmConversationMapper llmConversationMapper;
    private final ConversationService conversationService;
    private final LlmCallLogService llmCallLogService;
    private final LlmGateway llmGateway;
    private final ChatPromptBuilder chatPromptBuilder;
    private final ConversationContextManager conversationContextManager;
    private final SensitiveFilterService sensitiveFilterService;
    private final ChatFallbackService chatFallbackService;

    /**
     * 获取会话的消息列表，按时间升序返回。
     * 会验证当前用户是否有权限访问该会话
     */
    @Override
    public List<ChatMessageVO> listMessages(Long conversationId) {
        Long userId = getCurrentUserIdRequired();
        // 验证当前用户对该会话的访问权限，如果会话不存在或不属于当前用户，则抛出异常，防止越权访问。
        conversationService.getConversationEntity(conversationId, userId);

        // 查询数据库中属于该会话的消息记录，按照创建时间和ID升序排序，确保消息按照正确的顺序展示给前端。
        List<LlmMessage> messages = llmMessageMapper.selectList(new LambdaQueryWrapper<LlmMessage>().eq(LlmMessage::getConversationId, conversationId).orderByAsc(LlmMessage::getCreateTime).orderByAsc(LlmMessage::getId));

        return messages.stream().map(this::toVO).toList();
    }

    /**
     * 发送消息核心流程：
     * 1. 敏感检测
     * 2. 保存用户消息 + 合并景点上下文（事务1）
     * 3. 敏感内容直接返回拒绝回复
     * 4. 查询历史 -> 截断 -> 构建LLM请求 -> 调用LLM
     * 5. 保存LLM回复 + 更新会话统计信息（事务2）
     */
    @Override
    public ChatSendMessageVO sendMessage(Long conversationId, ChatSendMessageDTO dto) {
        Long userId = getCurrentUserIdRequired();
        LlmConversation conversation = conversationService.getConversationEntity(conversationId, userId);

        // 敏感检测，命中测过滤内容
        String originalContent = dto.getContent();
        boolean sensitive = sensitiveFilterService.isSensitive(originalContent);
        String filteredContent = sensitive ? sensitiveFilterService.filter(originalContent) : originalContent;

        // 事务1：保存用户信息 + 合并景点上下文
        LlmMessage userMessage = mergeContextAndSaveUserMessage(conversation, dto.getContextScenicId(), filteredContent, sensitive);
        // 首条用户消息自动识别会话类型（仅默认景点咨询时触发）
        autoClassifyConversationTypeIfNeeded(conversation, filteredContent, userMessage);
        // 首条用户消息自动生成标题摘要并覆盖默认标题
        autoGenerateConversationTitleIfNeeded(conversation, filteredContent, userMessage);

        // 敏感内容直接拒绝，不进入 LLM 调用。
        if (sensitive) {
            String rejectReply = "消息包含敏感内容，请修改后再试。";
            return saveAssistantResponse(conversation, userMessage, rejectReply, 0, null, "sensitive-filter", true);
        }

        // 拉取历史消息并按 token 截断
        Page<LlmMessage> page = new Page<>(1, HISTORY_FETCH_LIMIT);
        page.setSearchCount(false);
        List<LlmMessage> historyMessages = llmMessageMapper.selectPage(
                        page,
                        new LambdaQueryWrapper<LlmMessage>()
                                .eq(LlmMessage::getConversationId, conversationId)
                                .orderByDesc(LlmMessage::getId))
                .getRecords()
                .stream()
                .sorted(Comparator.comparing(LlmMessage::getId))
                .toList();
        historyMessages = truncateHistory(historyMessages);

        // 构建LLM请求对象
        LlmRequest request = chatPromptBuilder.buildChatRequest(
                userId, conversationId, conversation.getContextData(), historyMessages);

        // 调用 LLM 请求
        long start = System.currentTimeMillis();
        LlmResponse response;
        Long callLogId;
        String assistantReply;
        Integer tokenUsage;
        String modelName;
        try {
            // 调用 LLM 获取回复
            response = llmGateway.generate(request);
            // 记录成功调用日志
            callLogId = llmCallLogService.saveChatLog(userId, JsonUtils.toJson(request.getMessages()), response, LLMCallLogStatus.SUCCESS.getCode(), null, (int) (System.currentTimeMillis() - start));
            assistantReply = response.getContent();
            tokenUsage = response.getTotalTokens() == null ? 0 : response.getTotalTokens();
            modelName = response.getModelName();
        } catch (Exception ex) {
            // 记录失败调用日志
            llmCallLogService.saveChatLog(userId, JsonUtils.toJson(request.getMessages()), null, LLMCallLogStatus.FAILED.getCode(), ex.getMessage(), (int) (System.currentTimeMillis() - start));
            // 兜底回复
            assistantReply = chatFallbackService.getFallbackReply(ex);
            tokenUsage = 0;
            modelName = "fallback";
            callLogId = null;
        }

        // 事务2：保存 LLM 回复消息 + 更新会话统计信息
        return saveAssistantResponse(conversation, userMessage, assistantReply, tokenUsage, callLogId, modelName, false);
    }

    /**
     * 流式发送消息：
     * 1. 敏感检测
     * 2. 保存用户消息（事务1）
     * 3. 敏感内容直接返回拒绝
     * 4. 查询历史 -> 构建请求 -> 流式调用 LLM
     * 5. 流结束后保存完整回复（事务2）
     */
    @Override
    public Flux<String> sendMessageStream(Long conversationId, ChatSendMessageDTO dto) {
        Long userId = getCurrentUserIdRequired();
        LlmConversation conversation = conversationService.getConversationEntity(conversationId, userId);

        // 敏感检测
        String originalContent = dto.getContent();
        boolean sensitive = sensitiveFilterService.isSensitive(originalContent);
        String filteredContent = sensitive ? sensitiveFilterService.filter(originalContent) : originalContent;

        // 事务1：保存用户消息
        LlmMessage userMessage = mergeContextAndSaveUserMessage(conversation, dto.getContextScenicId(), filteredContent, sensitive);
        // 首条用户消息自动识别会话类型（仅默认景点咨询时触发）
        autoClassifyConversationTypeIfNeeded(conversation, filteredContent, userMessage);
        // 首条用户消息自动生成标题摘要并覆盖默认标题
        autoGenerateConversationTitleIfNeeded(conversation, filteredContent, userMessage);

        // 敏感内容直接拒绝
        if (sensitive) {
            String rejectReply = "消息包含敏感内容，请修改后再试。";
            saveAssistantResponse(conversation, userMessage, rejectReply, 0, null, "sensitive-filter", true);
            return Flux.just(rejectReply);
        }

        // 拉取历史消息
        Page<LlmMessage> page = new Page<>(1, HISTORY_FETCH_LIMIT);
        page.setSearchCount(false);
        List<LlmMessage> historyMessages = llmMessageMapper.selectPage(
                        page,
                        new LambdaQueryWrapper<LlmMessage>()
                                .eq(LlmMessage::getConversationId, conversationId)
                                .orderByDesc(LlmMessage::getId))
                .getRecords()
                .stream()
                .sorted(Comparator.comparing(LlmMessage::getId))
                .toList();
        historyMessages = truncateHistory(historyMessages);

        // 构建 LLM 请求
        LlmRequest request = chatPromptBuilder.buildChatRequest(
                userId, conversationId, conversation.getContextData(), historyMessages);

        // 流式调用 LLM
        StringBuilder fullReply = new StringBuilder();
        long start = System.currentTimeMillis();

        return llmGateway.generateStream(request)
                .doOnNext(fullReply::append)
                .doOnError(error -> {
                    // 记录失败日志
                    llmCallLogService.saveChatLog(userId, JsonUtils.toJson(request.getMessages()),
                            null, LLMCallLogStatus.FAILED.getCode(), error.getMessage(),
                            (int) (System.currentTimeMillis() - start));
                    // 保存兜底回复
                    Exception ex = error instanceof Exception ? (Exception) error : new Exception(error);
                    String fallbackReply = chatFallbackService.getFallbackReply(ex);
                    saveAssistantResponse(conversation, userMessage, fallbackReply, 0, null, "fallback", false);
                })
                .doOnComplete(() -> {
                    // 流结束后保存完整回复
                    String completeReply = fullReply.toString();
                    int tokenUsage = estimateTokens(completeReply);
                    // 记录成功日志
                    LlmResponse mockResponse = LlmResponse.builder()
                            .content(completeReply)
                            .totalTokens(tokenUsage)
                            .modelName(request.getModelName())
                            .build();
                    Long callLogId = llmCallLogService.saveChatLog(userId, JsonUtils.toJson(request.getMessages()),
                            mockResponse, LLMCallLogStatus.SUCCESS.getCode(), null,
                            (int) (System.currentTimeMillis() - start));
                    saveAssistantResponse(conversation, userMessage, completeReply, tokenUsage, callLogId,
                            request.getModelName(), false);
                })
                .onErrorResume(error -> {
                    // 返回兜底回复
                    Exception ex = error instanceof Exception ? (Exception) error : new Exception(error);
                    String fallbackReply = chatFallbackService.getFallbackReply(ex);
                    return Flux.just(fallbackReply);
                });
    }

    /**
     * 事务1：合并景点上下文 + 保存用户消息。
     * 两步操作需要原子性： 上下文更新成功但消息插入失败时需要一起回滚
     */
    private LlmMessage mergeContextAndSaveUserMessage(LlmConversation conversation,
                                                      Long contextScenicId,
                                                      String filteredContent,
                                                      boolean sensitive) {
        return executeInTransaction(() -> {
            // 1. 合并景点上下文
            mergeAndUpdateContext(conversation, contextScenicId);

            // 2. 保存用户消息
            LlmMessage message = buildMessage(conversation, filteredContent, MessageRole.USER.getRole(), MessageContentType.TEXT.getCode(), estimateTokens(filteredContent), null, sensitive);
            llmMessageMapper.insert(message);
            return message;
        });
    }

    /**
     * 若 contextScenicId 不为空，将景点信息合并到会话上下文并持久化。
     */
    private void mergeAndUpdateContext(LlmConversation conversation, Long contextScenicId) {
        if (contextScenicId == null) {
            return;
        }
        String mergedContext = conversationContextManager.mergeContext(
                conversation.getContextData(), contextScenicId);
        conversation.setContextData(mergedContext);
        llmConversationMapper.updateById(conversation);
    }

    /**
     * 会话类型自动分类：仅在首条用户消息，且当前为默认景点咨询类型时触发。
     */
    private void autoClassifyConversationTypeIfNeeded(LlmConversation conversation,
                                                      String userContent,
                                                      LlmMessage userMessage) {
        if (conversation == null || userMessage == null || !StringUtils.hasText(userContent)) {
            return;
        }
        Integer currentType = conversation.getConversationType();
        if (currentType == null || !currentType.equals(ConversationType.SCENIC_SPOT_INQUIRY.getCode())) {
            return;
        }
        Integer messageCount = conversation.getMessageCount();
        if (messageCount != null && messageCount > 0) {
            return;
        }
        Integer classifiedType = classifyConversationType(userContent);
        if (classifiedType == null || classifiedType.equals(currentType)) {
            return;
        }
        conversation.setConversationType(classifiedType);
        llmConversationMapper.updateById(conversation);
    }

    private Integer classifyConversationType(String userContent) {
        String classifyPrompt = "你是旅游会话分类器。请根据用户首条消息，将会话分类为以下代码之一：" +
                "1=智能客服（账号、登录、订单、售后、支付、平台规则、系统功能问题）；" +
                "2=行程规划（路线安排、天数、交通衔接、住宿搭配、时间规划）；" +
                "3=景点咨询（景点介绍、玩法、门票、开放时间、注意事项、对比推荐）。" +
                "仅返回一个数字：1 或 2 或 3，不要输出其他内容。";
        LlmRequest classifyRequest = LlmRequest.builder()
                .userId(0L)
                .conversationId(0L)
                .modelName(null)
                .jsonMode(false)
                .timeoutMs(3000)
                .messages(List.of(
                        LlmRequest.Message.builder().role("system").content(classifyPrompt).build(),
                        LlmRequest.Message.builder().role("user").content(userContent).build()))
                .build();
        try {
            LlmResponse response = llmGateway.generate(classifyRequest);
            if (response == null || !StringUtils.hasText(response.getContent())) {
                return null;
            }
            String raw = response.getContent().trim();
            String normalized = raw.replaceAll("[^123]", "");
            if (normalized.isEmpty()) {
                return null;
            }
            int code = Integer.parseInt(String.valueOf(normalized.charAt(0)));
            if (code < 1 || code > 3) {
                return null;
            }
            return code;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 会话标题自动摘要：仅在首条用户消息，且当前标题为默认标题时触发。
     */
    private void autoGenerateConversationTitleIfNeeded(LlmConversation conversation,
                                                        String userContent,
                                                        LlmMessage userMessage) {
        if (conversation == null || userMessage == null || !StringUtils.hasText(userContent)) {
            return;
        }
        Integer messageCount = conversation.getMessageCount();
        if (messageCount != null && messageCount > 0) {
            return;
        }
        String currentTitle = conversation.getTitle();
        if (!shouldAutoGenerateTitle(currentTitle)) {
            return;
        }
        String generatedTitle = generateConversationTitle(userContent);
        if (!StringUtils.hasText(generatedTitle)) {
            return;
        }
        conversation.setTitle(generatedTitle);
        llmConversationMapper.updateById(conversation);
    }

    private boolean shouldAutoGenerateTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return true;
        }
        String normalized = title.trim();
        return DEFAULT_CONVERSATION_TITLES.contains(normalized);
    }

    private String generateConversationTitle(String userContent) {
        String titlePrompt = "你是旅游会话标题生成器。请根据用户首条消息，生成一个简洁中文标题。"
                + "要求：8到16个字，不能包含标点符号、引号、换行，不要出现‘请问’‘帮我’等口语前缀，只输出标题本身。";
        LlmRequest titleRequest = LlmRequest.builder()
                .userId(0L)
                .conversationId(0L)
                .modelName(null)
                .jsonMode(false)
                .timeoutMs(3000)
                .messages(List.of(
                        LlmRequest.Message.builder().role("system").content(titlePrompt).build(),
                        LlmRequest.Message.builder().role("user").content(userContent).build()))
                .build();
        try {
            LlmResponse response = llmGateway.generate(titleRequest);
            if (response == null || !StringUtils.hasText(response.getContent())) {
                return null;
            }
            String title = sanitizeGeneratedTitle(response.getContent());
            return StringUtils.hasText(title) ? title : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String sanitizeGeneratedTitle(String rawTitle) {
        if (!StringUtils.hasText(rawTitle)) {
            return "";
        }
        String sanitized = rawTitle
                .replace("\n", "")
                .replace("\r", "")
                .replace("\"", "")
                .replace("“", "")
                .replace("”", "")
                .replace("'", "")
                .replace("，", "")
                .replace(",", "")
                .replace("。", "")
                .replace("！", "")
                .replace("？", "")
                .replace("：", "")
                .replace(":", "")
                .trim();
        if (!StringUtils.hasText(sanitized)) {
            return "";
        }
        if (sanitized.length() > AUTO_TITLE_MAX_LENGTH) {
            sanitized = sanitized.substring(0, AUTO_TITLE_MAX_LENGTH);
        }
        return sanitized.trim();
    }

    /**
     * 事务2：保存 LLM 回复消息 + 更新会话统计信息 + 封装 VO 返回.
     */
    private ChatSendMessageVO saveAssistantResponse(LlmConversation conversation,
                                                    LlmMessage userMessage,
                                                    String assistantReply,
                                                    int tokenUsage,
                                                    Long callLogId,
                                                    String modelName,
                                                    boolean isSensitiveReject) {
        return executeInTransaction(() -> {
            // LLM 消息本身不是用户输入，敏感标记固定为 false
            LlmMessage assistantMessage = buildMessage(
                    conversation,
                    assistantReply,
                    MessageRole.ASSISTANT.getRole(),
                    MessageContentType.TEXT.getCode(),
                    tokenUsage, callLogId, false);
            llmMessageMapper.insert(assistantMessage);

            if (isSensitiveReject) {
                // 敏感拒绝：只更新最后消息时间，不计入消息数和token统计
                conversation.setLastMessageAt(LocalDateTime.now());
                llmConversationMapper.updateById(conversation);
            } else {
                updateConversationAfterReply(conversation, tokenUsage);
            }

            return buildSendMessageVO(
                    userMessage, assistantMessage, assistantReply, tokenUsage, modelName);
        });
    }


    /**
     * 按 token 上限截断历史消息，优先保留最近的消息。
     * summarizeHistory 为摘要预留接口，当前返回空列表，后续可实现。
     */
    private List<LlmMessage> truncateHistory(List<LlmMessage> history) {
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }

        // 未超限则直接返回
        int total = history.stream().mapToInt(this::estimateTokens).sum();
        if (total <= HISTORY_MAX_TOKENS) {
            return history;
        }

        // 摘要压缩：截断历史 → 摘要消息，减少 token
        List<LlmMessage> summaryMessages = summarizeHistory(history);

        // 摘要已占用的 token
        int used = summaryMessages.stream().mapToInt(this::estimateTokens).sum();

        // 从最新消息往前贪心选取，addFirst 保证最终顺序为旧→新
        Deque<LlmMessage> selected = new ArrayDeque<>();
        for (int i = history.size() - 1; i >= 0; i--) {
            LlmMessage message = history.get(i);
            int cost = estimateTokens(message);
            if (used + cost > HISTORY_MAX_TOKENS) {
                break;   // 超限立即停止，保证消息连续性
            }
            selected.addFirst(message);
            used += cost;
        }

        List<LlmMessage> result = new ArrayList<>(summaryMessages);
        result.addAll(selected);
        return result;
    }

    /**
     * 摘要预留接口：当前返回空列表。
     * 后续实现时，可将被截断的早期消息压缩为一条摘要消息插入对话头部。
     */
    private List<LlmMessage> summarizeHistory(List<LlmMessage> history) {
        return Collections.emptyList();
    }

    /**
     * 估算消息 token 数：优先使用数据库中记录的真实值，否则按内容长度估算。
     */
    private int estimateTokens(LlmMessage message) {
        if (message == null) {
            return 0;
        }
        if (message.getTokensUsed() != null && message.getTokensUsed() > 0) {
            return message.getTokensUsed();
        }
        return estimateTokens(message.getContent());
    }

    /**
     * 按字符数估算 token 数：(length + 3) / 4，即 ceil(length / 4)，至少为 1。
     */
    private int estimateTokens(String content) {
        if (!StringUtils.hasText(content)) {
            return 1;
        }
        return Math.max(1, (content.length() + 3) / 4);
    }

    /**
     * 构建 LlmMessage 实体，ID 和创建时间由持久层自动填充。
     */
    private LlmMessage buildMessage(LlmConversation conversation, String content, String role, Integer contentType, Integer tokensUsed, Long callLogId, boolean sensitive) {
        LlmMessage message = new LlmMessage();
        message.setConversationId(conversation.getId());
        message.setRole(role);
        message.setContent(content);
        message.setContentType(contentType);
        message.setTokensUsed(tokensUsed == null ? 0 : tokensUsed);
        message.setLlmCallLogId(callLogId);
        message.setIsSensitive(sensitive ? SensitiveStatus.SENSITIVE.getCode() : SensitiveStatus.NOT_SENSITIVE.getCode());
        return message;
    }

    /**
     * 更新会话统计：消息数 +2，累加 token，更新最后消息时间。
     */
    private void updateConversationAfterReply(LlmConversation conversation, int replyTokens) {
        int newMessageCount = 2;
        conversation.setMessageCount(
                Objects.requireNonNullElse(conversation.getMessageCount(), 0) + newMessageCount);
        conversation.setTotalTokens(
                Objects.requireNonNullElse(conversation.getTotalTokens(), 0) + Math.max(replyTokens, 0));
        conversation.setLastMessageAt(LocalDateTime.now());
        llmConversationMapper.updateById(conversation);
    }

    /**
     * 在事务中执行业务逻辑，异常时自动回滚。
     * execute() 理论上不返回 null（supplier 始终有返回值），防御性校验兜底。
     */
    private <T> T executeInTransaction(Supplier<T> supplier) {
        T result = transactionTemplate.execute(status -> supplier.get());
        if (result == null) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "事务执行失败");
        }
        return result;
    }

    /**
     * 封装发送消息响应 VO。
     */
    private ChatSendMessageVO buildSendMessageVO(LlmMessage userMessage,
                                                 LlmMessage assistantMessage,
                                                 String replyContent,
                                                 int tokenUsage,
                                                 String modelName) {
        ChatSendMessageVO vo = new ChatSendMessageVO();
        vo.setUserMessageId(userMessage.getId());
        vo.setAssistantMessageId(assistantMessage.getId());
        vo.setReplyContent(replyContent);
        vo.setTokenUsage(tokenUsage);
        vo.setModelName(modelName);
        return vo;
    }

    /**
     * LlmMessage → ChatMessageVO。
     */
    private ChatMessageVO toVO(LlmMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setMessageId(message.getId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setContentType(message.getContentType());
        vo.setTokensUsed(message.getTokensUsed());
        vo.setLlmCallLogId(message.getLlmCallLogId());
        vo.setCreatedAt(message.getCreateTime());
        return vo;
    }

    /**
     * 获取当前登录用户ID，未登录则抛出异常。
     */
    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
