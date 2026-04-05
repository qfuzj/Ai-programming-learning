package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.history.BrowseHistoryCreateDTO;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.entity.UserBrowseHistory;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.UserBrowseHistoryMapper;
import com.travel.advisor.service.BrowseHistoryService;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.history.BrowseHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrowseHistoryServiceImpl implements BrowseHistoryService {

    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final ScenicSpotMapper scenicSpotMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void report(BrowseHistoryCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();
        ensureScenicExists(dto.getScenicId());

        UserBrowseHistory history = new UserBrowseHistory();
        history.setUserId(userId);
        history.setScenicSpotId(dto.getScenicId());
        history.setDurationSeconds(dto.getStayDuration() == null ? 0 : Math.max(0, dto.getStayDuration()));
        history.setSource(dto.getSource());
        history.setDeviceType(dto.getDeviceType());
        history.setBrowseTime(LocalDateTime.now());
        userBrowseHistoryMapper.insert(history);
    }

    @Override
    public PageResult<BrowseHistoryVO> page(PageQuery pageQuery) {
        Long userId = getCurrentUserIdRequired();
        Page<UserBrowseHistory> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<UserBrowseHistory> result = userBrowseHistoryMapper.selectPage(page, new LambdaQueryWrapper<UserBrowseHistory>()
            .eq(UserBrowseHistory::getUserId, userId)
            .orderByDesc(UserBrowseHistory::getBrowseTime)
            .orderByDesc(UserBrowseHistory::getId));

        List<UserBrowseHistory> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.<BrowseHistoryVO>builder()
                .records(Collections.emptyList())
                .total(result.getTotal())
                .pageNum(Math.toIntExact(result.getCurrent()))
                .pageSize(Math.toIntExact(result.getSize()))
                .totalPage(result.getPages())
                .build();
        }

        List<Long> scenicIds = records.stream().map(UserBrowseHistory::getScenicSpotId).distinct().toList();
        Map<Long, ScenicSpot> scenicMap = scenicSpotMapper.selectBatchIds(scenicIds).stream()
            .collect(Collectors.toMap(ScenicSpot::getId, scenic -> scenic));

        List<BrowseHistoryVO> vos = records.stream().map(item -> {
            ScenicSpot scenicSpot = scenicMap.get(item.getScenicSpotId());
            if (scenicSpot == null) {
                return null;
            }
            BrowseHistoryVO vo = new BrowseHistoryVO();
            vo.setId(item.getId());
            vo.setScenicId(item.getScenicSpotId());
            vo.setScenicName(scenicSpot.getName());
            vo.setCoverImage(scenicSpot.getCoverImage());
            vo.setStayDuration(item.getDurationSeconds());
            vo.setSource(item.getSource());
            vo.setDeviceType(item.getDeviceType());
            vo.setBrowseTime(item.getBrowseTime());
            return vo;
        }).filter(Objects::nonNull).toList();

        return PageResult.<BrowseHistoryVO>builder()
            .records(vos)
            .total(result.getTotal())
            .pageNum(Math.toIntExact(result.getCurrent()))
            .pageSize(Math.toIntExact(result.getSize()))
            .totalPage(result.getPages())
            .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteById(Long id) {
        Long userId = getCurrentUserIdRequired();
        int affected = userBrowseHistoryMapper.delete(new LambdaQueryWrapper<UserBrowseHistory>()
            .eq(UserBrowseHistory::getId, id)
            .eq(UserBrowseHistory::getUserId, userId));
        if (affected == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "浏览记录不存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void clear() {
        Long userId = getCurrentUserIdRequired();
        userBrowseHistoryMapper.delete(new LambdaQueryWrapper<UserBrowseHistory>()
            .eq(UserBrowseHistory::getUserId, userId));
    }

    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private void ensureScenicExists(Long scenicId) {
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(scenicId);
        if (scenicSpot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "景点不存在");
        }
    }
}
