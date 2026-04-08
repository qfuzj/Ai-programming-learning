package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.common.enums.LLMCallLogStatus;
import com.travel.advisor.common.enums.MessageContentType;
import com.travel.advisor.common.enums.SensitiveStatus;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.chat.ChatSendMessageDTO;
import com.travel.advisor.dto.llm.LlmChatRequest;
import com.travel.advisor.dto.llm.LlmChatResponse;
import com.travel.advisor.entity.LlmConversation;
import com.travel.advisor.entity.LlmMessage;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.llm.ConversationContextManager;
import com.travel.advisor.llm.LlmGateway;
import com.travel.advisor.llm.PromptBuilder;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 消息服务实现类，负责处理与聊天消息相关的业务逻辑，包括获取会话消息列表和发送消息到会话。实现了MessageService接口，提供listMessages方法获取指定会话的消息列表
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final LlmMessageMapper llmMessageMapper;
    private final LlmConversationMapper llmConversationMapper;
    private final ConversationService conversationService;
    private final LlmCallLogService llmCallLogService;
    private final LlmGateway llmGateway;
    private final PromptBuilder promptBuilder;
    private final ConversationContextManager conversationContextManager;

    /**
     * 获取指定会话的消息列表，首先验证当前用户对该会话的访问权限，然后查询数据库中属于该会话的消息记录，按照创建时间和ID升序排序，最后将消息实体转换为VO对象列表返回给前端展示。
     *
     * @param conversationId - 会话ID，由前端传入，表示要查询哪个会话的消息列表。
     * @return 指定会话的消息列表，每条消息包含ID、角色（用户或助手）、内容、内容类型、使用的token数量、关联的LLM调用日志ID、创建时间等信息。
     */
    @Override
    public List<ChatMessageVO> listMessages(Long conversationId) {
        Long userId = getCurrentUserIdRequired();
        // 验证当前用户对该会话的访问权限，如果会话不存在或不属于当前用户，则抛出异常，防止越权访问。
        conversationService.getConversationEntity(conversationId, userId);

        // 查询数据库中属于该会话的消息记录，按照创建时间和ID升序排序，确保消息按照正确的顺序展示给前端。
        List<LlmMessage> messages = llmMessageMapper.selectList(new LambdaQueryWrapper<LlmMessage>()
                .eq(LlmMessage::getConversationId, conversationId)
                .orderByAsc(LlmMessage::getCreateTime)
                .orderByAsc(LlmMessage::getId));

        return messages.stream().map(this::toVO).toList();
    }

    /**
     * 处理用户发送消息的核心逻辑，首先验证当前用户对该会话的访问权限，然后将用户输入的消息保存到数据库中，查询最近的消息历史构建LLM请求，调用LLM服务获取回复，将LLM回复保存到数据库中，并更新会话的统计信息，最后将用户消息ID、LLM回复消息ID、回复内容、消耗的token数量和模型名称等信息封装成VO对象返回给前端展示。
     *
     * @param conversationId - 会话ID，由前端传入，表示用户要发送消息到哪个会话。
     * @param dto            - 包含用户输入的消息内容和可选的上下文信息（如景点ID）的DTO对象，由前端传入，便于后端处理和生成回复。
     * @return 包含用户输入消息ID、LLM回复消息ID、回复内容、消耗的token数量和模型名称等信息的VO对象，便于前端展示和后续分析优化。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSendMessageVO sendMessage(Long conversationId, ChatSendMessageDTO dto) {
        Long userId = getCurrentUserIdRequired();
        // 验证当前用户对该会话的访问权限，如果会话不存在或不属于当前用户，则抛出异常，防止越权访问。
        LlmConversation conversation = conversationService.getConversationEntity(conversationId, userId);

        // 如果用户输入的消息带景点ID 则使用会话上下文管理器合并当前会话的上下文
        if (dto.getContextScenicId() != null) {
            String mergedContext = conversationContextManager.mergeContext(conversation.getContextData(), dto.getContextScenicId());
            // 更新会话的上下文信息到数据库中，便于后续构建LLM请求时使用最新的上下文数据，帮助LLM更好地理解用户的需求和对话背景。
            conversation.setContextData(mergedContext);
            llmConversationMapper.updateById(conversation);
        }

        // 将用户输入的消息保存到数据库中，记录消息的角色为"user"，内容类型为文本，使用的token数量初始为0，敏感状态为非敏感，便于后续统计和分析。
        LlmMessage userMessage = new LlmMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        userMessage.setContentType(MessageContentType.TEXT.getCode());
        userMessage.setTokensUsed(0);
        userMessage.setIsSensitive(SensitiveStatus.NOT_SENSITIVE.getCode());
        llmMessageMapper.insert(userMessage);

        // 查询最近的消息历史构建LLM请求，获取该会话最近20条消息记录，按照ID降序查询以获取最新的消息，然后再按照ID升序排序以确保消息顺序正确，最后将这些消息作为上下文信息构建LLM请求，便于LLM理解当前对话的上下文并生成合适的回复。
        List<LlmMessage> historyMessages = llmMessageMapper.selectList(new LambdaQueryWrapper<LlmMessage>()
                .eq(LlmMessage::getConversationId, conversationId)
                .orderByDesc(LlmMessage::getId)
                .last("limit 20"));
        if (historyMessages.isEmpty()) {
            historyMessages = Collections.emptyList();
        }
        historyMessages = historyMessages.stream()
                .sorted(Comparator.comparing(LlmMessage::getId))
                .toList();

        // 构建LLM请求对象，包含用户输入的消息内容、会话上下文数据和最近的消息历史，便于LLM理解当前对话的背景和用户的需求，从而生成更准确和相关的回复。
        LlmChatRequest request = promptBuilder.buildChatRequest(userId, conversationId, conversation.getContextData(), historyMessages);

        // 记录开始调用LLM服务的时间，便于后续计算调用耗时和性能分析。
        long start = System.currentTimeMillis();

        LlmChatResponse response;
        Long callLogId;
        try {
            // 调用LLM服务获取回复，将构建好的请求对象发送给LLM服务，获取LLM生成的回复内容、使用的token数量和模型名称等信息，便于后续保存回复消息和更新会话统计信息。
            response = llmGateway.chat(request);
            // 将LLM调用的请求和响应信息保存到调用日志中，记录调用状态为成功，便于后续分析LLM调用的效果和性能，以及排查可能出现的问题。
            callLogId = llmCallLogService.saveChatLog(
                    userId,
                    JsonUtils.toJson(request.getMessages()),
                    response,
                    LLMCallLogStatus.SUCCESS.getCode(),
                    null,
                    (int) (System.currentTimeMillis() - start)
            );
        } catch (Exception ex) {
            // 将LLM调用的请求和异常信息保存到调用日志中，记录调用状态为失败，便于后续分析LLM调用失败的原因和频率，以及改进系统的稳定性和用户体验。
            llmCallLogService.saveChatLog(
                    userId,
                    JsonUtils.toJson(request.getMessages()),
                    null,
                    LLMCallLogStatus.FAILED.getCode(),
                    ex.getMessage(),
                    (int) (System.currentTimeMillis() - start)
            );
            throw new BusinessException(ResultCode.LLM_CALL_FAILED, "AI 服务暂时不可用，请稍后重试");
        }

        // 将LLM回复保存到数据库中，记录消息的角色为"assistant"，内容类型为文本，使用的token数量和关联的LLM调用日志ID等信息，便于后续统计和分析LLM回复的效果和性能。
        LlmMessage assistantMessage = new LlmMessage();
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(response.getContent());
        assistantMessage.setContentType(MessageContentType.TEXT.getCode());
        assistantMessage.setTokensUsed(response.getTotalTokens());
        assistantMessage.setLlmCallLogId(callLogId);
        assistantMessage.setIsSensitive(SensitiveStatus.NOT_SENSITIVE.getCode());
        llmMessageMapper.insert(assistantMessage);

        // 本次交互产生两条消息：用户消息 + assistant回复
        int newMessageCount = 2;
        // 更新会话的统计信息，包括消息数量、总token数量和最后消息时间等，便于后续展示会话的基本信息和分析用户的使用习惯。
        conversation.setMessageCount(
                Objects.requireNonNullElse(conversation.getMessageCount(), 0) + newMessageCount
        );
        int replyTokens = response.getTotalTokens() == null ? 0 : response.getTotalTokens();
        conversation.setTotalTokens(
                Objects.requireNonNullElse(conversation.getTotalTokens(), 0) + replyTokens
        );
        conversation.setLastMessageAt(LocalDateTime.now());
        llmConversationMapper.updateById(conversation);

        // 封装用户消息ID、LLM回复消息ID、回复内容、消耗的token数量和模型名称等信息到VO对象中返回给前端展示，便于前端展示回复内容和相关信息，以及后续分析和优化。
        ChatSendMessageVO vo = new ChatSendMessageVO();
        vo.setUserMessageId(userMessage.getId());
        vo.setAssistantMessageId(assistantMessage.getId());
        vo.setReplyContent(response.getContent());
        vo.setTokenUsage(response.getTotalTokens());
        vo.setModelName(response.getModelName());
        return vo;
    }

    /**
     * 将 LlmMessage 实体转换为 ChatMessageVO 视图对象，便于前端展示消息信息。
     *
     * @param message - 数据库中的消息实体对象，包含消息的所有字段信息。
     * @return ChatMessageVO - 包含消息ID、角色（用户或助手）、内容、内容类型、使用的token数量、关联的LLM调用日志ID、创建时间等信息的视图对象。
     */
    private ChatMessageVO toVO(LlmMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setContentType(message.getContentType());
        vo.setTokensUsed(message.getTokensUsed());
        vo.setLlmCallLogId(message.getLlmCallLogId());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
