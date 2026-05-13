package com.travel.advisor.llm;

import com.travel.advisor.dto.llm.LlmRequest;
import com.travel.advisor.dto.llm.LlmResponse;
import reactor.core.publisher.Flux;

/**
 * LLM 网关接口，屏蔽底层模型调用细节。
 */
public interface LlmGateway {

    LlmResponse generate(LlmRequest request);

    /**
     * 流式生成响应，逐块返回内容
     */
    Flux<String> generateStream(LlmRequest request);
}
