package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.entity.UserFavorite;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.UserFavoriteMapper;
import com.travel.advisor.service.FavoriteService;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.favorite.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final ScenicSpotMapper scenicSpotMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addFavorite(Long scenicId) {
        Long userId = getCurrentUserIdRequired();
        ensureScenicExists(scenicId);
        UserFavorite existing = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
            .eq(UserFavorite::getUserId, userId)
            .eq(UserFavorite::getScenicSpotId, scenicId));
        if (existing != null) {
            throw new BusinessException(ResultCode.CONFLICT, "景点已收藏");
        }
        UserFavorite userFavorite = new UserFavorite();
        userFavorite.setUserId(userId);
        userFavorite.setScenicSpotId(scenicId);
        userFavorite.setFolderName("默认收藏");
        userFavoriteMapper.insert(userFavorite);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeFavorite(Long scenicId) {
        Long userId = getCurrentUserIdRequired();
        int affected = userFavoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
            .eq(UserFavorite::getUserId, userId)
            .eq(UserFavorite::getScenicSpotId, scenicId));
        if (affected == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "收藏记录不存在");
        }
    }

    @Override
    public PageResult<FavoriteVO> pageFavorites(PageQuery pageQuery) {
        Long userId = getCurrentUserIdRequired();
        Page<UserFavorite> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<UserFavorite> result = userFavoriteMapper.selectPage(page, new LambdaQueryWrapper<UserFavorite>()
            .eq(UserFavorite::getUserId, userId)
            .orderByDesc(UserFavorite::getCreateTime));

        List<UserFavorite> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.<FavoriteVO>builder()
                .records(Collections.emptyList())
                .total(result.getTotal())
                .pageNum(Math.toIntExact(result.getCurrent()))
                .pageSize(Math.toIntExact(result.getSize()))
                .totalPage(result.getPages())
                .build();
        }

        // 从用户收藏记录中提取景点ID，并批量查询景点信息，避免N+1查询问题
        List<Long> scenicIds = records.stream().map(UserFavorite::getScenicSpotId).distinct().toList();
        Map<Long, ScenicSpot> scenicMap = scenicSpotMapper.selectBatchIds(scenicIds).stream()
            .collect(Collectors.toMap(ScenicSpot::getId, scenic -> scenic));
        // 将用户收藏记录转换为VO对象，并填充景点信息
        List<FavoriteVO> vos = records.stream().map(item -> {
            ScenicSpot scenicSpot = scenicMap.get(item.getScenicSpotId());
            if (scenicSpot == null) {
                return null;
            }
            FavoriteVO vo = new FavoriteVO();
            vo.setScenicId(scenicSpot.getId());
            vo.setScenicName(scenicSpot.getName());
            vo.setCoverImage(scenicSpot.getCoverImage());
            vo.setScore(scenicSpot.getScore());
            vo.setFavoriteTime(item.getCreateTime());
            return vo;
        }).filter(Objects::nonNull).toList();

        return PageResult.<FavoriteVO>builder()
            .records(vos)
            .total(result.getTotal())
            .pageNum(Math.toIntExact(result.getCurrent()))
            .pageSize(Math.toIntExact(result.getSize()))
            .totalPage(result.getPages())
            .build();
    }

    /**
     * 获取当前请求的用户ID，如果未登录则抛出异常
     * @return 当前用户ID
     */
    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * 确保景点存在，如果不存在则抛出异常
     * @param scenicId 景点ID
     */
    private void ensureScenicExists(Long scenicId) {
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(scenicId);
        if (scenicSpot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "景点不存在");
        }
    }
}
