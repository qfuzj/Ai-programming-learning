package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.plan.TravelPlanCreateDTO;
import com.travel.advisor.dto.plan.TravelPlanItemCreateDTO;
import com.travel.advisor.dto.plan.TravelPlanQueryDTO;
import com.travel.advisor.entity.TravelPlan;
import com.travel.advisor.entity.TravelPlanItem;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.TravelPlanItemMapper;
import com.travel.advisor.mapper.TravelPlanMapper;
import com.travel.advisor.service.TravelPlanService;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.plan.TravelPlanDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TravelPlanServiceImpl implements TravelPlanService {

    private static final int PLAN_SOURCE_USER = 1;
    private static final int PLAN_STATUS_NORMAL = 1;

    private final TravelPlanMapper travelPlanMapper;
    private final TravelPlanItemMapper travelPlanItemMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createPlan(TravelPlanCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();

        TravelPlan plan = new TravelPlan();
        fillPlanFields(plan, dto);
        plan.setUserId(userId);
        plan.setSource(PLAN_SOURCE_USER);
        if (plan.getStatus() == null) {
            plan.setStatus(PLAN_STATUS_NORMAL);
        }
        plan.setViewCount(0);
        plan.setLikeCount(0);

        travelPlanMapper.insert(plan);
        return plan.getId();
    }

    @Override
    public PageResult<TravelPlanDetailVO> pageMyPlans(TravelPlanQueryDTO pageQuery) {
        Long userId = getCurrentUserIdRequired();

        Page<TravelPlan> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<TravelPlan> wrapper = new LambdaQueryWrapper<TravelPlan>()
                .eq(TravelPlan::getUserId, userId);
                
        if (pageQuery.getStatus() != null) {
            wrapper.eq(TravelPlan::getStatus, pageQuery.getStatus());
        }
        if (pageQuery.getIsPublic() != null) {
            wrapper.eq(TravelPlan::getIsPublic, pageQuery.getIsPublic());
        }
        if (pageQuery.getKeyword() != null && !pageQuery.getKeyword().trim().isEmpty()) {
            String keyword = pageQuery.getKeyword().trim();
            wrapper.and(w -> w.like(TravelPlan::getTitle, keyword)
                            .or()
                            .like(TravelPlan::getDescription, keyword));
        }
        
        wrapper.orderByDesc(TravelPlan::getCreateTime);

        Page<TravelPlan> result = travelPlanMapper.selectPage(page, wrapper);

        List<TravelPlanDetailVO> records = result.getRecords().stream().map(plan -> {
            TravelPlanDetailVO vo = new TravelPlanDetailVO();
            vo.setId(plan.getId());
            vo.setTitle(plan.getTitle());
            vo.setCoverImage(plan.getCoverImage());
            vo.setStartDate(plan.getStartDate());
            vo.setEndDate(plan.getEndDate());
            vo.setTotalDays(plan.getTotalDays());
            vo.setDestinationRegionId(plan.getDestinationRegionId());
            vo.setDescription(plan.getDescription());
            vo.setEstimatedBudget(plan.getEstimatedBudget());
            vo.setTravelCompanion(plan.getTravelCompanion());
            vo.setIsPublic(plan.getIsPublic());
            vo.setSource(plan.getSource());
            vo.setStatus(plan.getStatus());
            vo.setCreateTime(plan.getCreateTime());
            vo.setUpdateTime(plan.getUpdateTime());
            vo.setDays(Collections.emptyList());
            return vo;
        }).toList();

        return PageResult.<TravelPlanDetailVO>builder()
                .records(records)
                .total(result.getTotal())
                .pageNum(Math.toIntExact(result.getCurrent()))
                .pageSize(Math.toIntExact(result.getSize()))
                .totalPage(result.getPages())
                .build();
    }

    @Override
    public TravelPlanDetailVO getPlanDetail(Long id) {
        Long userId = getCurrentUserIdRequired();
        TravelPlan plan = getPlanByIdAndUser(id, userId);

        List<TravelPlanItem> items = travelPlanItemMapper.selectList(new LambdaQueryWrapper<TravelPlanItem>()
                .eq(TravelPlanItem::getTravelPlanId, plan.getId())
                .orderByAsc(TravelPlanItem::getDayNo)
                .orderByAsc(TravelPlanItem::getSortOrder)
                .orderByAsc(TravelPlanItem::getId));

        return toDetailVO(plan, items);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePlan(Long id, TravelPlanCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();
        TravelPlan existing = getPlanByIdAndUser(id, userId);

        TravelPlan update = new TravelPlan();
        update.setId(existing.getId());
        fillPlanFields(update, dto);
        travelPlanMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePlan(Long id) {
        Long userId = getCurrentUserIdRequired();
        TravelPlan existing = getPlanByIdAndUser(id, userId);

        travelPlanMapper.deleteById(existing.getId());
        travelPlanItemMapper.delete(new LambdaQueryWrapper<TravelPlanItem>()
                .eq(TravelPlanItem::getTravelPlanId, existing.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addPlanItem(Long planId, TravelPlanItemCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();
        getPlanByIdAndUser(planId, userId);

        TravelPlanItem item = new TravelPlanItem();
        item.setTravelPlanId(planId);
        item.setScenicSpotId(dto.getScenicSpotId());
        item.setDayNo(dto.getDayNo());
        item.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        item.setItemType(dto.getItemType() == null ? 5 : dto.getItemType());
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setStartTime(dto.getStartTime());
        item.setEndTime(dto.getEndTime());
        item.setLocation(dto.getLocation());
        item.setLongitude(dto.getLongitude());
        item.setLatitude(dto.getLatitude());
        item.setEstimatedCost(dto.getEstimatedCost());
        item.setNotes(dto.getNotes());

        travelPlanItemMapper.insert(item);
        return item.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePlanItem(Long planId, Long itemId) {
        Long userId = getCurrentUserIdRequired();
        getPlanByIdAndUser(planId, userId);

        int affected = travelPlanItemMapper.delete(new LambdaQueryWrapper<TravelPlanItem>()
                .eq(TravelPlanItem::getId, itemId)
                .eq(TravelPlanItem::getTravelPlanId, planId));
        if (affected == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "行程项不存在");
        }
    }

    private TravelPlan getPlanByIdAndUser(Long id, Long userId) {
        TravelPlan plan = travelPlanMapper.selectOne(new LambdaQueryWrapper<TravelPlan>()
                .eq(TravelPlan::getId, id)
                .eq(TravelPlan::getUserId, userId));
        if (plan == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "行程不存在");
        }
        return plan;
    }

    /**
     * 将TravelPlan实体对象和对应的行程项列表转换为TravelPlanDetailVO对象，适用于行程详情展示时的字段映射和数据封装。该方法会从TravelPlan对象中提取相关字段值，并将其赋值给TravelPlanDetailVO对象的相应属性。同时，通过调用groupByDay方法将行程项列表按照天数进行分组，并将分组后的结果设置到VO对象的days属性中，以便在前端页面上展示每天的行程安排。
     *
     * @param plan  需要转换的TravelPlan实体对象，包含了行程的基本信息，如标题、日期、预算等。该对象通常是从数据库中查询得到的，包含了行程的完整数据。
     * @param items 行程项列表，包含了行程中的所有活动信息，如景点、餐饮、住宿等。每个行程项都包含了dayNo字段，表示该活动属于行程中的第几天。该列表通常是从数据库中查询得到的，包含了行程的所有相关活动数据。
     * @return
     */
    private TravelPlanDetailVO toDetailVO(TravelPlan plan, List<TravelPlanItem> items) {
        TravelPlanDetailVO vo = new TravelPlanDetailVO();
        vo.setId(plan.getId());
        vo.setTitle(plan.getTitle());
        vo.setCoverImage(plan.getCoverImage());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setTotalDays(plan.getTotalDays());
        vo.setDestinationRegionId(plan.getDestinationRegionId());
        vo.setDescription(plan.getDescription());
        vo.setEstimatedBudget(plan.getEstimatedBudget());
        vo.setTravelCompanion(plan.getTravelCompanion());
        vo.setIsPublic(plan.getIsPublic());
        vo.setSource(plan.getSource());
        vo.setStatus(plan.getStatus());
        vo.setCreateTime(plan.getCreateTime());
        vo.setUpdateTime(plan.getUpdateTime());
        vo.setDays(groupByDay(items));
        return vo;
    }

    /**
     * 将行程项列表按照dayNo字段进行分组，生成每天的行程安排。每个TravelPlanDayVO对象表示行程中的一天，包含当天的行程项列表。通过这种分组方式，用户可以清晰地看到每天的行程安排，并且系统可以根据这些信息提供更精准的推荐和优化建议。
     *
     * @param items 行程项列表，包含了行程中的所有活动信息，如景点、餐饮、住宿等。每个行程项都包含了dayNo字段，表示该活动属于行程中的第几天。
     * @return 按照dayNo分组后的行程安排列表，每个TravelPlanDayVO对象包含了当天的行程项列表，按照sortOrder字段排序，确保用户看到的行程安排是按照计划的顺序展示的。
     */
    private List<TravelPlanDetailVO.TravelPlanDayVO> groupByDay(List<TravelPlanItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, List<TravelPlanItem>> grouped = new LinkedHashMap<>();
        for (TravelPlanItem item : items) {
            grouped.computeIfAbsent(item.getDayNo(), key -> new ArrayList<>()).add(item);
        }

        List<TravelPlanDetailVO.TravelPlanDayVO> days = new ArrayList<>();
        for (Map.Entry<Integer, List<TravelPlanItem>> entry : grouped.entrySet()) {
            TravelPlanDetailVO.TravelPlanDayVO dayVO = new TravelPlanDetailVO.TravelPlanDayVO();
            dayVO.setDayNo(entry.getKey());
            dayVO.setItems(entry.getValue().stream().map(this::toItemVO).toList());
            days.add(dayVO);
        }
        return days;
    }

    /**
     * 将TravelPlanItem实体对象转换为TravelPlanItemVO对象，适用于行程详情展示时的字段映射。该方法会从TravelPlanItem对象中提取相关字段值，并将其赋值给TravelPlanItemVO对象的相应属性，以便在前端页面上展示行程项的详细信息。
     *
     * @param item 需要转换的TravelPlanItem实体对象，包含了行程项的具体信息，如景点名称、时间安排、位置等。该对象通常是从数据库中查询得到的，包含了行程项的完整数据。
     * @return 转换后的TravelPlanItemVO对象，包含了行程项的详细信息，适用于在行程详情页面上展示。该VO对象会被封装在TravelPlanDayVO中，按照天数和顺序展示给用户。
     */
    private TravelPlanDetailVO.TravelPlanItemVO toItemVO(TravelPlanItem item) {
        TravelPlanDetailVO.TravelPlanItemVO itemVO = new TravelPlanDetailVO.TravelPlanItemVO();
        itemVO.setId(item.getId());
        itemVO.setScenicSpotId(item.getScenicSpotId());
        itemVO.setDayNo(item.getDayNo());
        itemVO.setSortOrder(item.getSortOrder());
        itemVO.setItemType(item.getItemType());
        itemVO.setTitle(item.getTitle());
        itemVO.setDescription(item.getDescription());
        itemVO.setStartTime(item.getStartTime());
        itemVO.setEndTime(item.getEndTime());
        itemVO.setLocation(item.getLocation());
        itemVO.setLongitude(item.getLongitude());
        itemVO.setLatitude(item.getLatitude());
        itemVO.setEstimatedCost(item.getEstimatedCost());
        itemVO.setNotes(item.getNotes());
        itemVO.setCreateTime(item.getCreateTime());
        return itemVO;
    }

    /**
     * 将TravelPlanCreateDTO中的字段值填充到TravelPlan实体中，适用于创建和更新行程时的字段赋值。该方法会根据DTO中的字段值设置TravelPlan的相应属性，包括标题、封面图片、日期、预算等信息。同时，对于一些可选字段（如isPublic和status），如果DTO中没有提供值，则会设置默认值，确保行程数据的完整性和一致性。
     *
     * @param plan 需要填充字段的TravelPlan实体对象，通常是在创建或更新行程时使用。该对象会被赋值DTO中的字段值，并最终保存到数据库中。
     * @param dto  包含行程信息的TravelPlanCreateDTO对象，用户通过API请求提交的行程数据会被封装在该DTO中。该方法会从DTO中提取字段值，并将其赋值给TravelPlan实体对象的相应属性。
     */
    private void fillPlanFields(TravelPlan plan, TravelPlanCreateDTO dto) {
        plan.setTitle(dto.getTitle());
        plan.setCoverImage(dto.getCoverImage());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setTotalDays(dto.getTotalDays());
        plan.setDestinationRegionId(dto.getDestinationRegionId());
        plan.setDescription(dto.getDescription());
        plan.setEstimatedBudget(dto.getEstimatedBudget());
        plan.setTravelCompanion(dto.getTravelCompanion());
        plan.setIsPublic(dto.getIsPublic() == null ? 0 : dto.getIsPublic());
        if (dto.getStatus() != null) {
            plan.setStatus(dto.getStatus());
        }
    }

    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
