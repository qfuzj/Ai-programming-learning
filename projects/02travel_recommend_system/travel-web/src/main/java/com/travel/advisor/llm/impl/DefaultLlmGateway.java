package com.travel.advisor.llm.impl;

import com.travel.advisor.llm.LlmGateway;
import com.travel.advisor.llm.LlmProperties;
import com.travel.advisor.dto.llm.LlmChatRequest;
import com.travel.advisor.dto.llm.LlmChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 默认的 LLM 网关实现，提供一个模拟的聊天响应。实际应用中可以替换为调用真实 LLM 服务的实现。
 */
@Component
@RequiredArgsConstructor
public class DefaultLlmGateway implements LlmGateway {

    private final LlmProperties llmProperties;

    @Override
    public LlmChatResponse chat(LlmChatRequest request) {
        String reply = "[模拟回复] 已收到你的问题，我会结合你的上下文给出旅行建议。";
        int inputTokens = request.getMessages() == null ? 0 : request.getMessages().size() * 20;
        int outputTokens = 60;
        return LlmChatResponse.builder()
                .content(reply)
                .modelName(request.getModelName() == null ? llmProperties.getModelName() : request.getModelName())
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalTokens(inputTokens + outputTokens)
                .build();
    }
}
