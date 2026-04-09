package com.travel.advisor.llm;

import com.travel.advisor.common.enums.ChatFallbackStrategy;
import org.springframework.stereotype.Component;

/**
 * 对话失败降级骨架实现。
 */
@Component
public class ChatFallbackService {

    /**
     * LLM 调用失败时返回兜底回复。
     */
    public String getFallbackReply(Exception ex) {
        return ChatFallbackStrategy.fromException(ex).getReplyText();
    }
}
