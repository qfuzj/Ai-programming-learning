package com.travel.advisor.llm;

import com.travel.advisor.dto.llm.LlmChatRequest;
import com.travel.advisor.dto.llm.LlmChatResponse;

/**
 * LLM 网关接口，定义了与大语言模型交互的方法。
 */
public interface LlmGateway {

    LlmChatResponse chat(LlmChatRequest request);
}
