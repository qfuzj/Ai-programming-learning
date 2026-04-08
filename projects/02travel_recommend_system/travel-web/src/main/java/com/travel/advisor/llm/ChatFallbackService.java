package com.travel.advisor.llm;

import org.springframework.stereotype.Component;

/**
 * 对话失败降级骨架实现。
 */
@Component
public class ChatFallbackService {

    public String fallbackReply() {
        return "当前服务繁忙，请稍后重试，或尝试简化你的问题。";
    }
}
