package com.travel.advisor.service;

import com.travel.advisor.dto.llm.LlmChatResponse;

public interface LlmCallLogService {

    Long saveCallLog(Long userId,
                    String callType,
                    String requestPrompt,
                    String requestMessages,
                    LlmChatResponse response,
                    Integer status,
                    String errorMessage,
                    Integer responseTimeMs);

    Long saveChatLog(Long userId,
                    String requestMessages,
                    LlmChatResponse response,
                    Integer status,
                    String errorMessage,
                    Integer responseTimeMs);
}
