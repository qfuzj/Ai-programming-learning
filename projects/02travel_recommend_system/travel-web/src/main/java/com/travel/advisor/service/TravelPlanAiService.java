package com.travel.advisor.service;

import com.travel.advisor.dto.plan.TravelPlanAiGenerateDTO;
import com.travel.advisor.vo.plan.TravelPlanDetailVO;

public interface TravelPlanAiService {

    TravelPlanDetailVO generateDraft(TravelPlanAiGenerateDTO dto);
}
