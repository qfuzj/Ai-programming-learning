package com.travel.advisor.utils;

import lombok.experimental.UtilityClass;

/**
 * 文件资源 ID 解析相关工具方法。
 * <p>
 * 业务库中诸如 {@code scenic_spot.cover_image}、{@code tag.icon}、{@code user.avatar}
 * 等字段历史上既存放过裸 URL，也存放着 {@code file_resource.id}。本工具集中处理
 * 「字符串 → fileResourceId(Long)」的判断与解析逻辑，避免各业务模块重复
 * {@code trim + matches("\\d+") + Long.parseLong + try/catch} 模板代码。
 */
@UtilityClass
public class FileResourceIds {

    /**
     * 尝试把字符串解析为 fileResourceId。
     * <ul>
     *   <li>{@code null}/空白 → 返回 {@code null}</li>
     *   <li>纯数字（trim 后） → 返回对应 {@link Long}</li>
     *   <li>含非数字字符（典型如旧版 URL） → 返回 {@code null}</li>
     * </ul>
     * 该方法只判定「是否像一个 ID」，不做存在性校验。
     */
    public static Long tryParseId(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || !trimmed.chars().allMatch(Character::isDigit)) {
            return null;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            // 超出 Long 范围；按"非合法 ID"处理
            return null;
        }
    }
}
