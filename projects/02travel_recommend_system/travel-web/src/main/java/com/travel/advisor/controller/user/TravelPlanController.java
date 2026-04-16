package com.travel.advisor.controller.user;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.plan.TravelPlanCreateDTO;
import com.travel.advisor.dto.plan.TravelPlanCreateWithItemsDTO;
import com.travel.advisor.dto.plan.TravelPlanQueryDTO;
import com.travel.advisor.dto.plan.TravelPlanItemCreateDTO;
import com.travel.advisor.service.TravelPlanService;
import com.travel.advisor.vo.plan.TravelPlanDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/travel-plans")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @PostMapping
    public Result<Long> createPlan(@Valid @RequestBody TravelPlanCreateDTO dto) {
        return Result.success(travelPlanService.createPlan(dto));
    }

    @PostMapping("/with-items")
    public Result<Long> createPlanWithItems(@Valid @RequestBody TravelPlanCreateWithItemsDTO dto) {
        return Result.success(travelPlanService.createPlanWithItems(dto.getPlan(), dto.getItems()));
    }

    @GetMapping
    public Result<PageResult<TravelPlanDetailVO>> pageMyPlans(@Valid TravelPlanQueryDTO pageQuery) {
        return Result.success(travelPlanService.pageMyPlans(pageQuery));
    }

    @GetMapping("/{id}")
    public Result<TravelPlanDetailVO> getPlanDetail(@PathVariable Long id) {
        return Result.success(travelPlanService.getPlanDetail(id));
    }

    @PutMapping("/{id}")
    public Result<Void> updatePlan(@PathVariable Long id, @Valid @RequestBody TravelPlanCreateDTO dto) {
        travelPlanService.updatePlan(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        travelPlanService.deletePlan(id);
        return Result.success();
    }

    @PostMapping("/{id}/items")
    public Result<Long> addPlanItem(@PathVariable Long id, @Valid @RequestBody TravelPlanItemCreateDTO dto) {
        return Result.success(travelPlanService.addPlanItem(id, dto));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public Result<Void> deletePlanItem(@PathVariable Long id, @PathVariable Long itemId) {
        travelPlanService.deletePlanItem(id, itemId);
        return Result.success();
    }
}
