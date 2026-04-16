package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件资源状态枚举
 */
@Getter
@AllArgsConstructor
public enum FileResourceStatus {

    TEMP(0, "临时"),
    USED(1, "已使用"),
    DELETED(2, "已删除");

    private final Integer code;
    private final String desc;

    public static FileResourceStatus fromCode(Integer code) {
        for (FileResourceStatus status : FileResourceStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的文件资源状态代码: " + code);
    }
}
