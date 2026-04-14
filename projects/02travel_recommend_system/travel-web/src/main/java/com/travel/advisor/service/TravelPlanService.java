package com.travel.advisor.service;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.plan.TravelPlanCreateDTO;
import com.travel.advisor.dto.plan.TravelPlanQueryDTO;
import com.travel.advisor.dto.plan.TravelPlanItemCreateDTO;
import com.travel.advisor.vo.plan.TravelPlanDetailVO;

public interface TravelPlanService {

    Long createPlan(TravelPlanCreateDTO dto);

    PageResult<TravelPlanDetailVO> pageMyPlans(TravelPlanQueryDTO pageQuery);

    TravelPlanDetailVO getPlanDetail(Long id);

    void updatePlan(Long id, TravelPlanCreateDTO dto);

    void deletePlan(Long id);

    Long addPlanItem(Long planId, TravelPlanItemCreateDTO dto);

    void deletePlanItem(Long planId, Long itemId);
}
