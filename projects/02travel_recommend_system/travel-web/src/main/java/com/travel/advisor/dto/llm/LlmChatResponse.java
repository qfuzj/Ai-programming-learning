package com.travel.advisor.dto.llm;

import lombok.Builder;
import lombok.Data;

/**
 * LLM聊天响应对象，包含生成的内容、使用的模型名称以及输入输出的Token数量等信息，用于接收LLM返回的聊天结果。
 */
@Data
@Builder
public class LlmChatResponse {

    /**
     * LLM生成的回复内容，前端可以直接展示给用户，或者根据需要进行进一步处理（如格式化、提取关键信息等）
     */
    private String content;

    private String modelName;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;
}
