package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.enums.TravelPlanItemType;
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

    // 来源：1 用户创建 2 LLM 生成 3 系统推荐
    private static final int PLAN_SOURCE_USER = 1;
    // 0 草稿 1 正常 2 已完成
    private static final int PLAN_STATUS_NORMAL = 1;
    private static final int ITEM_TYPE_CUSTOM = TravelPlanItemType.CUSTOM.getCode();

    private final TravelPlanMapper travelPlanMapper;
    private final TravelPlanItemMapper travelPlanItemMapper;

    /**
     * 创建行程计划，首先获取当前用户ID，并验证用户是否登录。然后根据传入的TravelPlanCreateDTO对象创建一个新的TravelPlan实体对象，并将相关字段值填充到该对象中。接着设置行程计划的来源为用户创建，默认状态为正常，并初始化浏览量和点赞数为0。最后将行程计划保存到数据库中，并返回新创建的行程计划ID。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createPlan(TravelPlanCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();

        TravelPlan plan = new TravelPlan();
        fillPlanFieldsForCreate(plan, dto);
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

    /**
     * 分页查询当前用户的行程计划
     * 首先获取当前用户ID，并验证用户是否登录。
     * 然后根据传入的TravelPlanQueryDTO对象构建一个分页查询条件，包括行程计划的状态、公开性和关键词等信息。
     * 接着使用MyBatis-Plus的分页查询功能从数据库中查询符合条件的行程计划列表，
     * 并将查询结果转换为TravelPlanDetailVO对象列表。
     * 最后将转换后的VO对象列表封装到PageResult对象中，并返回给调用方，以便在前端页面上展示分页后的行程计划数据。
     */
    @Override
    public PageResult<TravelPlanDetailVO> pageMyPlans(TravelPlanQueryDTO pageQuery) {
        Long userId = getCurrentUserIdRequired();
        validatePageSize(pageQuery.getPageSize());

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

        List<TravelPlanDetailVO> records = result.getRecords().stream()
                .map(plan -> {
                    TravelPlanDetailVO vo = new TravelPlanDetailVO();
                    fillBaseInfo(plan, vo);
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

    /**
     * 获取当前用户ID，并验证用户是否登录。如果用户未登录，则抛出一个业务异常，提示用户需要登录才能进行相关操作。该方法通常在需要获取当前用户信息的服务方法中调用，以确保只有登录用户才能访问和操作与其相关的数据。
     */
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

    /**
     * 获取当前用户ID，并验证用户是否登录。
     * 如果用户未登录，则抛出一个业务异常，提示用户需要登录才能进行相关操作。
     * 该方法通常在需要获取当前用户信息的服务方法中调用，以确保只有登录用户才能访问和操作与其相关的数据。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePlan(Long id, TravelPlanCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();
        TravelPlan existing = getPlanByIdAndUser(id, userId);

        TravelPlan update = new TravelPlan();
        update.setId(existing.getId());
        fillPlanFieldsForUpdate(update, dto);
        travelPlanMapper.updateById(update);
    }

    /**
     * 删除行程计划，首先获取当前用户ID，并验证用户是否登录。
     * 然后根据行程计划ID和用户ID查询行程计划，确保该行程计划属于当前用户。
     * 如果查询结果为空，则抛出一个业务异常，提示行程计划不存在。
     * 接着删除与该行程计划关联的所有行程项，最后删除行程计划本身。
     * 该方法通过事务管理确保在删除过程中数据的一致性和完整性，如果在删除过程中发生任何异常，事务将会回滚，避免数据出现不一致的情况。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePlan(Long id) {
        Long userId = getCurrentUserIdRequired();
        TravelPlan existing = getPlanByIdAndUser(id, userId);

        travelPlanItemMapper.delete(new LambdaQueryWrapper<TravelPlanItem>()
                .eq(TravelPlanItem::getTravelPlanId, existing.getId()));
        travelPlanMapper.deleteById(existing.getId());
    }

    /**
     * 添加行程项，首先获取当前用户ID，并验证用户是否登录。
     * 然后根据行程计划ID和用户ID查询行程计划，确保该行程计划属于当前用户。
     * 接着验证行程项的dayNo字段是否在行程计划的总天数范围内，如果不在范围内，则抛出一个业务异常，提示dayNo无效。
     * 然后根据传入的TravelPlanItemCreateDTO对象创建一个新的TravelPlanItem实体对象，并将相关字段值填充到该对象中，包括关联的行程计划ID、天数、排序顺序等信息。
     * 最后将行程项保存到数据库中，并返回新创建的行程项ID。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addPlanItem(Long planId, TravelPlanItemCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();
        TravelPlan plan = getPlanByIdAndUser(planId, userId);
        validateDayNoInRange(plan, dto.getDayNo());

        TravelPlanItem item = new TravelPlanItem();
        item.setTravelPlanId(planId);
        fillPlanItemFields(item, planId, dto);
        travelPlanItemMapper.insert(item);
        return item.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePlanItem(Long planId, Long itemId, TravelPlanItemCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();
        TravelPlan plan = getPlanByIdAndUser(planId, userId);
        validateDayNoInRange(plan, dto.getDayNo());

        TravelPlanItem existing = travelPlanItemMapper.selectOne(new LambdaQueryWrapper<TravelPlanItem>()
                .eq(TravelPlanItem::getId, itemId)
                .eq(TravelPlanItem::getTravelPlanId, planId));
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "行程项不存在");
        }

        TravelPlanItem update = new TravelPlanItem();
        update.setId(existing.getId());
        update.setTravelPlanId(planId);
        fillPlanItemFields(update, planId, dto);
        // 编辑场景下若用户未显式传 sortOrder 且未改 startTime，fillPlanItemFields 会根据
        // "同日最后一项 + 1" 推算出一个与现值无关的新顺序号，会把当前项莫名排到末尾。
        // 因此显式保留原 sortOrder，避免编辑仅修改标题/备注时位置发生漂移。
        if (dto.getSortOrder() == null && dto.getStartTime() == null) {
            update.setSortOrder(existing.getSortOrder());
        }
        travelPlanItemMapper.updateById(update);
    }

    /**
     * 删除行程项，首先获取当前用户ID，并验证用户是否登录。
     */
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

    /**
     * 根据行程计划ID和用户ID查询行程计划，确保该行程计划属于当前用户。
     * 如果查询结果为空，则抛出一个业务异常，提示行程计划不存在。
     * 该方法通常在需要验证用户对行程计划的访问权限时调用，以确保用户只能访问和操作属于自己的行程计划数据。
     */
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
        fillBaseInfo(plan, vo);
        vo.setDays(groupByDay(items));
        return vo;
    }

    private void fillBaseInfo(TravelPlan plan, TravelPlanDetailVO vo) {
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
        vo.setCreatedAt(plan.getCreateTime());
        vo.setUpdatedAt(plan.getUpdateTime());
    }

    private void fillPlanItemFields(TravelPlanItem item, Long planId, TravelPlanItemCreateDTO dto) {
        Integer itemType = resolveItemType(dto.getItemType());
        item.setScenicSpotId(dto.getScenicSpotId());
        item.setDayNo(dto.getDayNo());
        item.setSortOrder(resolveItemSortOrder(planId, dto));
        item.setItemType(itemType);
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setStartTime(dto.getStartTime());
        item.setEndTime(dto.getEndTime());
        item.setLocation(dto.getLocation());
        item.setLongitude(dto.getLongitude());
        item.setLatitude(dto.getLatitude());
        item.setEstimatedCost(dto.getEstimatedCost());
        item.setNotes(dto.getNotes());
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
        itemVO.setCreatedAt(item.getCreateTime());
        return itemVO;
    }

    /**
     * 将TravelPlanCreateDTO中的字段值填充到TravelPlan实体中，适用于创建和更新行程时的字段赋值。该方法会根据DTO中的字段值设置TravelPlan的相应属性，包括标题、封面图片、日期、预算等信息。同时，对于一些可选字段（如isPublic和status），如果DTO中没有提供值，则会设置默认值，确保行程数据的完整性和一致性。
     *
     * @param plan 需要填充字段的TravelPlan实体对象，通常是在创建或更新行程时使用。该对象会被赋值DTO中的字段值，并最终保存到数据库中。
     * @param dto  包含行程信息的TravelPlanCreateDTO对象，用户通过API请求提交的行程数据会被封装在该DTO中。该方法会从DTO中提取字段值，并将其赋值给TravelPlan实体对象的相应属性。
     */
    private void fillPlanFieldsForCreate(TravelPlan plan, TravelPlanCreateDTO dto) {
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

    /**
     * 将TravelPlanCreateDTO中的字段值填充到TravelPlan实体中，适用于更新行程时的字段赋值。
     * 该方法会根据DTO中的字段值设置TravelPlan的相应属性，包括标题、封面图片、日期、预算等信息。
     * 同时，对于一些可选字段（如isPublic和status），如果DTO中没有提供值，则不会修改原有的值，确保行程数据的合理更新。
     */
    private void fillPlanFieldsForUpdate(TravelPlan plan, TravelPlanCreateDTO dto) {
        plan.setTitle(dto.getTitle());
        plan.setCoverImage(dto.getCoverImage());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setTotalDays(dto.getTotalDays());
        plan.setDestinationRegionId(dto.getDestinationRegionId());
        plan.setDescription(dto.getDescription());
        plan.setEstimatedBudget(dto.getEstimatedBudget());
        plan.setTravelCompanion(dto.getTravelCompanion());
        if (dto.getIsPublic() != null) {
            plan.setIsPublic(dto.getIsPublic());
        }
        if (dto.getStatus() != null) {
            plan.setStatus(dto.getStatus());
        }
    }

    /**
     * 验证行程项的dayNo字段是否在行程计划的总天数范围内，确保用户添加的行程项对应的天数不会超过整个行程计划的总天数。该方法会检查TravelPlan对象中的totalDays字段和传入的dayNo参数，如果dayNo大于totalDays，则抛出一个业务异常，提示用户天数超出计划范围。通过这种验证，可以保证行程数据的合理性和一致性，避免出现不符合逻辑的行程安排。
     */
    private void validateDayNoInRange(TravelPlan plan, Integer dayNo) {
        if (plan.getTotalDays() == null || dayNo == null) {
            return;
        }
        if (dayNo > plan.getTotalDays()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "天数超出计划范围");
        }
    }

    private Integer resolveItemSortOrder(Long planId, TravelPlanItemCreateDTO dto) {
        if (dto.getSortOrder() != null) {
            return dto.getSortOrder();
        }
        if (dto.getStartTime() != null) {
            return dto.getStartTime().getHour() * 60 + dto.getStartTime().getMinute();
        }
        TravelPlanItem lastItem = travelPlanItemMapper.selectOne(new LambdaQueryWrapper<TravelPlanItem>()
                .eq(TravelPlanItem::getTravelPlanId, planId)
                .eq(TravelPlanItem::getDayNo, dto.getDayNo())
                .orderByDesc(TravelPlanItem::getSortOrder)
                .orderByDesc(TravelPlanItem::getId)
                .last("limit 1"));
        if (lastItem == null || lastItem.getSortOrder() == null) {
            return 0;
        }
        return lastItem.getSortOrder() + 1;
    }

    /**
     * 验证分页查询的pageSize参数，确保用户请求的每页数据量不会超过系统设定的最大值。该方法会检查传入的pageSize参数，如果pageSize不为null且大于100，则抛出一个业务异常，提示用户pageSize不能大于100。通过这种验证，可以防止用户请求过多的数据导致系统性能问题，同时也可以引导用户合理使用分页功能，提升系统的稳定性和响应速度。
     * 
     * @param pageSize
     */
    private void validatePageSize(Integer pageSize) {
        if (pageSize != null && pageSize > 100) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "pageSize 不能大于 100");
        }
    }

    /**
     * 解析行程项的类型，确保传入的itemType参数是合法的行程项类型。如果itemType为null，则默认设置为自定义类型（ITEM_TYPE_CUSTOM）。如果itemType不为null，则会调用TravelPlanItemType枚举类的fromCode方法验证该类型是否合法，如果不合法则抛出一个业务异常，提示用户itemType非法。通过这种方式，可以保证行程项的数据合法性和系统的一致性，避免出现不符合预期的行程项类型。
     */
    private Integer resolveItemType(Integer itemType) {
        if (itemType == null) {
            return ITEM_TYPE_CUSTOM;
        }
        if (TravelPlanItemType.fromCode(itemType) == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "itemType 非法");
        }
        return itemType;
    }

    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
