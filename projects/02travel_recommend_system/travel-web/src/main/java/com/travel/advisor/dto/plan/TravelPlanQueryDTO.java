package com.travel.advisor.dto.plan;

import com.travel.advisor.common.page.PageQuery;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 行程计划分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TravelPlanQueryDTO extends PageQuery {

    private Integer status;

    private Integer isPublic;

    @AssertTrue(message = "pageSize 不能大于 100")
    public boolean isPageSizeWithinLimit() {
        return getPageSize() == null || getPageSize() <= 100;
    }
}
