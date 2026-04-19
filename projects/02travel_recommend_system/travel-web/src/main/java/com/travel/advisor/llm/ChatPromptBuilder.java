package com.travel.advisor.llm;

import com.travel.advisor.dto.llm.LlmRequest;
import com.travel.advisor.entity.LlmMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatPromptBuilder 负责构建聊天场景使用的 LLM 请求。
 */
@Component
@RequiredArgsConstructor
public class ChatPromptBuilder {

    private final LlmProperties llmProperties;
    private final ConversationContextManager conversationContextManager;

    /**
     * 构建聊天场景的 LLM 请求，包含系统提示、历史消息和上下文信息。
     *
     * @param userId          用户 ID
     * @param conversationId  会话 ID
     * @param contextData     最新的上下文数据（如用户偏好、历史行为等），用于构建系统提示
     * @param historyMessages 历史消息列表，按照时间顺序排列
     * @return 构建好的 LlmRequest 对象，准备发送给 LLM 进行处理
     */
    public LlmRequest buildChatRequest(Long userId,
                                       Long conversationId,
                                       String contextData,
                                       List<LlmMessage> historyMessages) {
        List<LlmRequest.Message> messages = new ArrayList<>();

        // 系统提示：提供上下文信息和对话指导，帮助 LLM 理解用户的需求和对话背景
        messages.add(LlmRequest.Message.builder()
                .role("system")
                .content(conversationContextManager.buildSystemContextPrompt(contextData))
                .build());

        // 历史消息：按照时间顺序添加用户和助手的历史对话，帮助 LLM 理解当前对话状态和用户需求
        for (LlmMessage historyMessage : historyMessages) {
            messages.add(LlmRequest.Message.builder()
                    .role(historyMessage.getRole())
                    .content(historyMessage.getContent())
                    .build());
        }

        return LlmRequest.builder()
                .userId(userId)
                .conversationId(conversationId)
                .modelName(llmProperties.getModelName())
                .jsonMode(false)
                .timeoutMs(llmProperties.getTimeoutMs())
                .messages(messages)
                .build();
    }
}
