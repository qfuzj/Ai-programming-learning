package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    private final TransactionTemplate transactionTemplate;
    private final LlmMessageMapper llmMessageMapper;
    private final LlmConversationMapper llmConversationMapper;
    private final ConversationService conversationService;
    private final LlmCallLogService llmCallLogService;
    private final LlmGateway llmGateway;
    private final PromptBuilder promptBuilder;
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
        LlmRequest request = promptBuilder.buildChatRequest(
                userId, conversationId, conversation.getContextData(), historyMessages);

        // 调用 LLM 请求
        long start = System.currentTimeMillis();
        LlmResponse response;
        Long callLogId;
        String assistantReply;
        Integer tokenUsage;
        String modelName;
        try {
            // 调用LLM服务获取回复，将构建好的请求对象发送给LLM服务，获取LLM生成的回复内容、使用的token数量和模型名称等信息，便于后续保存回复消息和更新会话统计信息。
            response = llmGateway.generate(request);
            // 将LLM调用的请求和响应信息保存到调用日志中，记录调用状态为成功，便于后续分析LLM调用的效果和性能，以及排查可能出现的问题。
            callLogId = llmCallLogService.saveChatLog(userId, JsonUtils.toJson(request.getMessages()), response, LLMCallLogStatus.SUCCESS.getCode(), null, (int) (System.currentTimeMillis() - start));
            assistantReply = response.getContent();
            tokenUsage = response.getTotalTokens() == null ? 0 : response.getTotalTokens();
            modelName = response.getModelName();
        } catch (Exception ex) {
            // 将LLM调用的请求和异常信息保存到调用日志中，记录调用状态为失败，便于后续分析LLM调用失败的原因和频率，以及改进系统的稳定性和用户体验。
            llmCallLogService.saveChatLog(userId, JsonUtils.toJson(request.getMessages()), null, LLMCallLogStatus.FAILED.getCode(), ex.getMessage(), (int) (System.currentTimeMillis() - start));
            // 获取LLM调用失败的兜底回复内容，便于在LLM服务不可用或发生异常时，仍能为用户提供有意义的回复，提升系统的鲁棒性和用户体验。
            assistantReply = chatFallbackService.getFallbackReply(ex);
            tokenUsage = 0;
            modelName = "fallback";
            callLogId = null;
        }

        // 事务2：保存 LLM 回复消息 + 更新会话统计信息
        return saveAssistantResponse(conversation, userMessage, assistantReply, tokenUsage, callLogId, modelName, false);
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

        // 计算历史消息的总token数量，如果不超过预设的最大token限制，则直接返回原始历史消息列表，避免不必要的处理和性能开销。
        int total = history.stream().mapToInt(this::estimateTokens).sum();
        if (total <= HISTORY_MAX_TOKENS) {
            return history;
        }

        // 摘要接口预留：将被截断的历史消息压缩成摘要消息，便于保留对话上下文的连续性，同时减少token数量，优化LLM调用的效果和性能。
        List<LlmMessage> summaryMessages = summarizeHistory(history);

        // 摘要消息的token数量，如果摘要消息的token数量已经接近或超过最大token限制，则直接返回摘要消息，避免后续添加历史消息导致超限。
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
