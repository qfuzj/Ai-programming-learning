package com.travel.advisor.service;

import com.travel.advisor.dto.llm.LlmChatResponse;

public interface LlmCallLogService {

    Long saveChatLog(Long userId,
                    String requestMessages,
                    LlmChatResponse response,
                    Integer status,
                    String errorMessage,
                    Integer responseTimeMs);
}
