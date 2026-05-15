package com.travel.advisor.service.portrait;

/**
 * 用户画像分析器：基于浏览/收藏/点评行为推断旅行风格、预算水平、偏好季节、兴趣关键词等核心画像字段，并写回 user_profile 表。
 */
public interface UserPortraitAnalyzer {

    /**
     * 同步分析并落库画像核心字段（不含 summary）。
     */
    void analyzeAndPersist(Long userId);

    /**
     * 取 Redis 中由 LLM 生成的动态摘要；不存在时返回 null。
     */
    String getCachedSummary(Long userId);

    /**
     * 异步调用 LLM 生成更自然的画像摘要并写入 Redis；调用失败不影响调用方。
     */
    void asyncRefreshSummary(Long userId, PortraitSummaryContext context);
}
