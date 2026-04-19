package com.travel.advisor.dto.plan;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 旅行计划AI生成的行程草稿数据结构，包含行程标题、描述和每天的行程安排等信息，供前端展示和后续编辑使用
  * 1. title 字段表示行程的标题，便于用户识别和管理不同的行程计划
  * 2. description 字段提供了行程的整体描述，帮助用户快速了解行程的主要内容和亮点
  * 3. days 字段是一个列表，每个元素表示一天的行程安排，包含当天的编号和具体的活动项列表
  * 4. 每个活动项（ItemPayload）包含了活动类型、相关景点ID、标题、描述、时间安排、地点、预计费用和备注等详细信息，帮助用户全面了解每天的行程安排
  * 5. 这个数据结构可以根据实际需要进行扩展，比如添加更多的字段来描述行程的其他方面（如交通方式、住宿信息等），或者添加一些辅助方法来处理行程数据（如计算总费用、生成行程摘要等）
 */
@Data
public class TravelPlanAiDraftPayload {

    private String title;

    private String description;

    private List<DayPayload> days;

    @Data
    public static class DayPayload {

        private Integer dayNo;

        private List<ItemPayload> items;
    }

    @Data
    public static class ItemPayload {

        private Integer itemType;

        private Long scenicSpotId;

        private String title;

        private String description;

        private String startTime;

        private String endTime;

        private String location;

        private BigDecimal estimatedCost;

        private String notes;
    }
}
