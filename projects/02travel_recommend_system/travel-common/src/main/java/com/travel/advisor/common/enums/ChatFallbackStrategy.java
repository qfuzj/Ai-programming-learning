package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * 对话失败降级策略枚举，定义不同异常场景下的兜底回复文案。
 */
@Getter
@AllArgsConstructor
public enum ChatFallbackStrategy {

    TIMEOUT(
            "AI 响应超时，请稍后重试"
    ),
    RATE_LIMIT(
            "当前咨询人数较多，请稍等片刻再试"
    ),
    SERVICE_UNAVAILABLE(
            "AI 服务维护中，建议您稍后再来或联系人工客服"
    ),
    DEFAULT(
            "抱歉，暂时无法回答您的问题，请稍后重试"
    );

    /**
     * 用户可见的兜底回复文案
     */
    private final String replyText;

    /**
     * 根据异常类型推断降级策略
     */
    public static ChatFallbackStrategy fromException(Exception ex) {
        if (ex == null) {
            return DEFAULT;
        }
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();

        // 超时：SocketTimeoutException / TimeoutException / 消息含 timeout/timed out
        if (ex instanceof SocketTimeoutException
                || ex instanceof TimeoutException
                || msg.contains("timeout")
                || msg.contains("timed out")) {
            return TIMEOUT;
        }

        // 限流：消息含 rate limit / too many requests / 429
        if (msg.contains("rate limit")
                || msg.contains("too many requests")
                || msg.contains("429")) {
            return RATE_LIMIT;
        }

        // 服务不可用：消息含 unavailable / 503 / connection refused
        if (msg.contains("unavailable")
                || msg.contains("503")
                || msg.contains("connection refused")) {
            return SERVICE_UNAVAILABLE;
        }

        return DEFAULT;
    }
}