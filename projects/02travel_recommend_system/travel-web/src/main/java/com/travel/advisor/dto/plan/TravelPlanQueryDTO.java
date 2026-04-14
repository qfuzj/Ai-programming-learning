package com.travel.advisor.dto.plan;

import com.travel.advisor.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TravelPlanQueryDTO extends PageQuery {

    private Integer status;

    private Integer isPublic;
}
