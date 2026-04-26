package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.entity.RecommendRecord;
import com.travel.advisor.entity.RecommendResultItem;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.entity.UserFavorite;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.RecommendRecordMapper;
import com.travel.advisor.mapper.RecommendResultItemMapper;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.UserFavoriteMapper;
import com.travel.advisor.service.FavoriteService;
import com.travel.advisor.service.FileService;
import com.travel.advisor.utils.FileResourceIds;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.favorite.FavoriteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    /** 收藏反馈回写的时间窗口：仅对该用户最近 N 天内产生的推荐记录回写 is_favorited 标志。 */
    private static final int RECOMMEND_FEEDBACK_LOOKBACK_DAYS = 30;

    private final UserFavoriteMapper userFavoriteMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final RecommendRecordMapper recommendRecordMapper;
    private final RecommendResultItemMapper recommendResultItemMapper;
    private final FileService fileService;

    /**
     * 添加收藏
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addFavorite(Long scenicId) {
        Long userId = getCurrentUserIdRequired();
        ensureScenicExists(scenicId);
        
        // 连同逻辑删除的记录一起查询
        UserFavorite existing = userFavoriteMapper.selectWithDeleted(userId, scenicId);
        
        if (existing != null) {
            if (existing.getIsDeleted() != null && existing.getIsDeleted() == 0) {
                throw new BusinessException(ResultCode.CONFLICT, "景点已收藏");
            }
            // 如果已经被逻辑删除了，我们走恢复逻辑
            userFavoriteMapper.restoreDeleted(existing.getId());
        } else {
            // 否则走全新插入；并发场景下依赖 (user_id, scenic_spot_id) 唯一索引兜底
            UserFavorite userFavorite = new UserFavorite();
            userFavorite.setUserId(userId);
            userFavorite.setScenicSpotId(scenicId);
            userFavorite.setFolderName("默认收藏");
            try {
                userFavoriteMapper.insert(userFavorite);
            } catch (DuplicateKeyException e) {
                // 同用户并发收藏同景点，认为已收藏，直接抛冲突，避免重复 +1
                throw new BusinessException(ResultCode.CONFLICT, "景点已收藏");
            }
        }
        
        // 更新景点的收藏数量
        scenicSpotMapper.update(null, new LambdaUpdateWrapper<ScenicSpot>()
                .eq(ScenicSpot::getId, scenicId)
                .setSql("favorite_count = favorite_count + 1"));

        // 反向回写推荐反馈：把该用户最近推荐过该景点的 result_item 标记为已收藏
        syncRecommendFavoriteFlag(userId, scenicId, 1);
    }

    /**
     * 取消收藏
      * 1. 获取当前用户ID，确保用户已登录
      * 2. 验证景点ID是否存在，确保取消收藏的记录关联到有效的景点
      * 3. 删除用户的收藏记录，如果记录不存在则抛出异常
      * 4. 更新景点的收藏数量，确保数量不为负数
     */
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
        
        // 更新景点的收藏数量
        scenicSpotMapper.update(null, new LambdaUpdateWrapper<ScenicSpot>()
                .eq(ScenicSpot::getId, scenicId)
                .setSql("favorite_count = CASE WHEN favorite_count > 0 THEN favorite_count - 1 ELSE 0 END"));

        // 反向回写推荐反馈：把该用户最近推荐过该景点的 result_item 的 is_favorited 置回 0
        syncRecommendFavoriteFlag(userId, scenicId, 0);
    }

    /**
     * 清空收藏
      * 1. 获取当前用户ID，确保用户已登录
      * 2. 查找用户所有已收藏的景点ID，以便更新数量
      * 3. 删除用户的全部收藏
      * 4. 批量更新相关景点的收藏数量，确保数量不为负数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void clearFavorites() {
        Long userId = getCurrentUserIdRequired();
        
        // 查找用户所有已收藏的景点ID，以便更新数量
        List<UserFavorite> favorites = userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
            .eq(UserFavorite::getUserId, userId));
            
        if (favorites.isEmpty()) {
            return;
        }
        
        // 删除用户的全部收藏
        userFavoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
            .eq(UserFavorite::getUserId, userId));
            
        // 一次 UPDATE ... WHERE id IN (...) 完成所有景点收藏数递减，避免 N 次单条更新
        List<Long> scenicIds = favorites.stream()
                .map(UserFavorite::getScenicSpotId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!scenicIds.isEmpty()) {
            scenicSpotMapper.update(null, new LambdaUpdateWrapper<ScenicSpot>()
                    .in(ScenicSpot::getId, scenicIds)
                    .setSql("favorite_count = CASE WHEN favorite_count > 0 THEN favorite_count - 1 ELSE 0 END"));
        }
    }

    /**
     * 分页查询用户的收藏列表，返回包含景点信息的VO对象列表
      * 1. 获取当前用户ID，确保用户已登录
      * 2. 分页查询用户的收藏记录，并根据景点ID批量查询景点信息，避免N+1查询问题
      * 3. 将查询结果转换为VO对象列表，并返回分页结果
     */
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
        // 批量解析封面图 fileResourceId → URL（景点表现存储为 fileResourceId，纯数字才需解析）
        List<Long> coverFileIds = scenicMap.values().stream()
            .map(ScenicSpot::getCoverImage)
            .map(FileResourceIds::tryParseId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, String> coverUrlMap = fileService.resolveUrls(coverFileIds);
        // 将用户收藏记录转换为VO对象，并填充景点信息
        List<FavoriteVO> vos = records.stream().map(item -> {
            ScenicSpot scenicSpot = scenicMap.get(item.getScenicSpotId());
            if (scenicSpot == null) {
                return null;
            }
            FavoriteVO vo = new FavoriteVO();
            vo.setScenicId(scenicSpot.getId());
            vo.setScenicName(scenicSpot.getName());
            vo.setCoverImage(resolveCover(scenicSpot.getCoverImage(), coverUrlMap));
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
     * 反向回写推荐反馈：将该用户最近 {@link #RECOMMEND_FEEDBACK_LOOKBACK_DAYS} 天内
     * 推荐过该景点的 {@code recommend_result_item} 记录标记为 / 恢复为未收藏，
     * 从而把"景点列表收藏 / 取消收藏"与"推荐链路的收藏反馈"打通。
     *
     * <p>异常仅记录日志不抛出——收藏主流程不应因反馈写回失败而失败。
     * 数据体量较大时该更新以 userId + scenicId + 时间窗口过滤，SQL 成本受控。
     *
     * @param userId   当前用户 ID
     * @param scenicId 目标景点 ID
     * @param flag     1 = 已收藏；0 = 取消收藏
     */
    private void syncRecommendFavoriteFlag(Long userId, Long scenicId, int flag) {
        try {
            // 查找最近 N 天内该用户的推荐记录，获取其 ID 列表
            LocalDateTime since = LocalDateTime.now().minusDays(RECOMMEND_FEEDBACK_LOOKBACK_DAYS);
            List<Long> recentRecordIds = recommendRecordMapper.selectList(
                    new LambdaQueryWrapper<RecommendRecord>()
                            .select(RecommendRecord::getId)
                            .eq(RecommendRecord::getUserId, userId)
                            .ge(RecommendRecord::getCreateTime, since))
                    .stream()
                    .map(RecommendRecord::getId)
                    .toList();
            if (recentRecordIds.isEmpty()) {
                return;
            }
            // 批量更新这些推荐记录中对应景点的 result_item 的 is_favorited 标志
            recommendResultItemMapper.update(null,
                    new LambdaUpdateWrapper<RecommendResultItem>()
                            .in(RecommendResultItem::getRecommendRecordId, recentRecordIds)
                            .eq(RecommendResultItem::getScenicSpotId, scenicId)
                            .ne(RecommendResultItem::getIsFavorited, flag)
                            .set(RecommendResultItem::getIsFavorited, flag));
        } catch (Exception e) {
            // 反馈回写是旁路副作用，失败不影响收藏主流程
            log.warn("syncRecommendFavoriteFlag failed, userId={}, scenicId={}, flag={}, err={}",
                    userId, scenicId, flag, e.getMessage());
        }
    }

    /**
     * 将景点封面图字段（fileResourceId 或旧 URL）解析为可访问 URL。
     * 为空返回空串；非数字认为旧版 URL 原样返回。
     */
    private String resolveCover(String coverImage, Map<Long, String> coverUrlMap) {
        if (coverImage == null || coverImage.isBlank()) {
            return "";
        }
        Long fileId = FileResourceIds.tryParseId(coverImage);
        if (fileId == null) {
            return coverImage.trim();
        }
        return coverUrlMap.getOrDefault(fileId, "");
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
