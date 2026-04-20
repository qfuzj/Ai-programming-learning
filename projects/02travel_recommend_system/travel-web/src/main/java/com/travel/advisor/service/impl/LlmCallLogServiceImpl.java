package com.travel.advisor.service.impl;

import com.travel.advisor.entity.LlmCallLog;
import com.travel.advisor.llm.LlmProperties;
import com.travel.advisor.dto.llm.LlmResponse;
import com.travel.advisor.mapper.LlmCallLogMapper;
import com.travel.advisor.service.LlmCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** LLM 调用日志服务实现 */
@Service
@RequiredArgsConstructor
public class LlmCallLogServiceImpl implements LlmCallLogService {

    private final LlmCallLogMapper llmCallLogMapper;
    private final LlmProperties llmProperties;

    /** 保存 LLM 调用日志，返回日志 ID */
    @Override
    public Long saveCallLog(Long userId,
                            String callType,
                            String requestPrompt,
                            String requestMessages,
                            LlmResponse response,
                            Integer status,
                            String errorMessage,
                            Integer responseTimeMs) {
        LlmCallLog log = new LlmCallLog();
        log.setUserId(userId);
        log.setCallType(callType);
        log.setModelName(response == null ? llmProperties.getModelName() : response.getModelName());
        log.setProvider(llmProperties.getProvider());
        log.setRequestPrompt(requestPrompt);
        log.setRequestMessages(requestMessages);
        log.setResponseContent(response == null || response.getContent() == null ? "" : response.getContent());
        log.setInputTokens(response == null || response.getInputTokens() == null ? 0 : response.getInputTokens());
        log.setOutputTokens(response == null || response.getOutputTokens() == null ? 0 : response.getOutputTokens());
        log.setTotalTokens(response == null || response.getTotalTokens() == null ? 0 : response.getTotalTokens());
        log.setCostAmount(BigDecimal.ZERO);
        log.setResponseTimeMs(responseTimeMs);
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        log.setRetryCount(0);
        llmCallLogMapper.insert(log);
        return log.getId();
    }

    @Override
    public Long saveChatLog(Long userId,
                            String requestMessages,
                            LlmResponse response,
                            Integer status,
                            String errorMessage,
                            Integer responseTimeMs) {
        return saveCallLog(userId, "chat", null, requestMessages, response, status, errorMessage, responseTimeMs);
    }
}
