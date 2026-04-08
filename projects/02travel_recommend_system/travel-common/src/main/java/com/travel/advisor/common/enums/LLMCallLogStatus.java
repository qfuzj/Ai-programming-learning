package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * LLM调用日志状态枚举，表示一次LLM调用的结果状态。
 */
@Getter
@AllArgsConstructor
public enum LLMCallLogStatus {

    FAILED(0, "失败"),
    SUCCESS(1, "成功"),
    TIMEOUT(2, "超时"),
    RATE_LIMIT(3, "限流");

    private final int code;
    private final String desc;

    public static LLMCallLogStatus fromCode(int code) {
        for (LLMCallLogStatus status : LLMCallLogStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的LLM调用日志状态码: " + code);
    }

}
