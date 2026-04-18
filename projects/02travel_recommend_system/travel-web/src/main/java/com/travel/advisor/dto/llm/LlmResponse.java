package com.travel.advisor.dto.llm;

import lombok.Builder;
import lombok.Data;

/**
 * 通用 LLM 响应对象。
 */
@Data
@Builder
public class LlmResponse {

    /**
     * LLM生成的回复内容，前端可以直接展示给用户，或者根据需要进行进一步处理（如格式化、提取关键信息等）
     */
    private String content;

    private String modelName;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;
}
