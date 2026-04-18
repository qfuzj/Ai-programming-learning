package com.travel.advisor.service;

import com.travel.advisor.dto.llm.LlmResponse;

public interface LlmCallLogService {

    Long saveCallLog(Long userId,
                    String callType,
                    String requestPrompt,
                    String requestMessages,
                    LlmResponse response,
                    Integer status,
                    String errorMessage,
                    Integer responseTimeMs);

    Long saveChatLog(Long userId,
                    String requestMessages,
                    LlmResponse response,
                    Integer status,
                    String errorMessage,
                    Integer responseTimeMs);
}
