package com.travel.advisor.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * LLM调用分析结果VO类，用于封装LLM调用的统计数据和趋势信息，供前端展示使用。
 */
@Data
public class LlmAnalysisVO {

    /**
     * 总调用次数
     */
    private Long totalCallCount;

    /**
     * 成功调用次数
     */
    private Long successCallCount;

    /**
     * 失败调用次数
     */
    private Long failCallCount;

    /**
     * 总消耗的Token数量
     */
    private Long totalTokens;

    /**
     * 总消耗金额
     */
    private BigDecimal totalCostAmount;


    // ==================== 趋势图数据（与 dates 下标一一对应） ====================

    /**
     * 日期列表，格式 yyyy-MM-dd，作为趋势图 X 轴
     * 示例：["2026-01-01", "2026-01-02", "2026-01-03"]
     */
    private List<String> dates;

    /**
     * 每日调用次数，与 dates 下标一一对应，作为趋势图 Y 轴
     * 示例：[120, 98, 135]
     */
    private List<Long> callCounts;

    /**
     * 每日消耗金额（元），与 dates 下标一一对应，作为趋势图 Y 轴
     * 示例：[0.24, 0.19, 0.27]
     */
    private List<BigDecimal> costAmounts;
}
