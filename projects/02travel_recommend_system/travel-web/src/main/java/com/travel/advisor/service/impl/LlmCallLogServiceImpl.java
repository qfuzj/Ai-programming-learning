package com.travel.advisor.service.impl;

import com.travel.advisor.entity.LlmCallLog;
import com.travel.advisor.llm.LlmProperties;
import com.travel.advisor.dto.llm.LlmResponse;
import com.travel.advisor.mapper.LlmCallLogMapper;
import com.travel.advisor.service.LlmCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * LLM调用日志服务实现类，负责将每次与LLM交互的请求和响应信息记录到数据库中，便于后续分析和优化。实现了LlmCallLogService接口，提供保存聊天日志的方法，将用户ID、请求消息、LLM响应、调用状态、错误信息和响应时间等关键信息保存到LlmCallLog实体中，并通过LlmCallLogMapper将日志记录插入到数据库中。
 */
@Service
@RequiredArgsConstructor
public class LlmCallLogServiceImpl implements LlmCallLogService {

    private final LlmCallLogMapper llmCallLogMapper;
    private final LlmProperties llmProperties;

    /**
     * 保存LLM调用日志，记录用户ID、调用类型、请求提示、请求消息、LLM响应内容、状态、错误信息和响应时间等信息，便于后续分析和优化LLM调用情况
      * 1. 创建一个新的LlmCallLog对象，并设置各个字段的值，包括用户ID、调用类型、模型名称、提供商、请求提示、请求消息、响应内容、输入输出Token数量、总Token数量、调用成本金额、响应时间、状态、错误信息和重试次数等
      * 2. 使用llmCallLogMapper将日志对象插入到数据库中，完成日志记录的保存
      * 3. 返回保存后的日志ID，便于调用方进行后续查询和分析
     */
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
