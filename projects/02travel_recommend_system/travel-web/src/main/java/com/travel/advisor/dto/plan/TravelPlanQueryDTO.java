package com.travel.advisor.dto.plan;

import com.travel.advisor.common.page.PageQuery;
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
}
