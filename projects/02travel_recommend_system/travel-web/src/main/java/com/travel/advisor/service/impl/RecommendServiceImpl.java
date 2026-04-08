package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.common.enums.RecommendType;
import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.domain.recommend.RankedRecommend;
import com.travel.advisor.domain.recommend.RecallCandidate;
import com.travel.advisor.domain.recommend.RecallContext;
import com.travel.advisor.entity.RecommendRecord;
import com.travel.advisor.entity.RecommendResultItem;
import com.travel.advisor.entity.ScenicSpotTag;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.RecommendRecordMapper;
import com.travel.advisor.mapper.RecommendResultItemMapper;
import com.travel.advisor.mapper.ScenicSpotTagMapper;
import com.travel.advisor.service.RecommendService;
import com.travel.advisor.service.impl.strategy.RecallStrategy;
import com.travel.advisor.service.recommend.RecommendRankService;
import com.travel.advisor.service.recommend.RecommendReasonBuilder;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.utils.RedisUtils;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.recommend.RecommendItemVO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    // 首页推荐和相似推荐的缓存时间设置为60分钟，可以根据实际情况调整
    private static final Duration HOME_CACHE_TTL = Duration.ofMinutes(60);
    private static final Duration SIMILAR_CACHE_TTL = Duration.ofMinutes(60);
    private static final String TAG_STRATEGY_NAME = "TAG";

    private final List<RecallStrategy> recallStrategies;
    private final RecommendRankService recommendRankService;
    private final RecommendReasonBuilder recommendReasonBuilder;
    private final RecommendRecordMapper recommendRecordMapper;
    private final RecommendResultItemMapper recommendResultItemMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;
    private final RedisUtils redisUtils;

    /**
     * 首页推荐接口，基于用户画像和历史行为等信息，结合多种召回策略进行推荐，并对结果进行排序和分页展示。
     *
     * @param pageQuery 分页查询参数，包含页码和每页大小等信息，用于控制返回结果的分页展示
     * @return 分页结果，包含推荐结果列表、总记录数、当前页码、每页大小和总页数等信息，供前端展示使用
     */
    @Override
    public PageResult<RecommendItemVO> homeRecommend(PageQuery pageQuery) {
        // 获取当前用户ID，确保用户已登录，否则抛出未授权异常
        Long userId = getCurrentUserIdRequired();

        // 构建缓存键，格式为 "recommend:user:{userId}:home:{pageNum}:{pageSize}"，用于在Redis中存储和查询推荐结果的缓存
        String cacheKey = "recommend:user:" + userId + ":home:" + pageQuery.getPageNum() + ":" + pageQuery.getPageSize();

        // 尝试从Redis缓存中获取推荐结果，如果存在且不为空，则直接返回缓存中的结果，避免重复计算和数据库查询，提高响应速度
        String cacheValue = redisUtils.get(cacheKey);
        if (cacheValue != null && !cacheValue.isBlank()) {
            RecommendCachePayload payload = JsonUtils.fromJson(cacheValue, RecommendCachePayload.class);
            return PageResult.<RecommendItemVO>builder()
                    .records(payload.getRecords() == null ? Collections.emptyList() : payload.getRecords())
                    .total(payload.getTotal())
                    .pageNum(pageQuery.getPageNum())
                    .pageSize(pageQuery.getPageSize())
                    .totalPage(payload.getTotalPage())
                    .build();
        }

        // 记录推荐请求的开始时间，用于后续计算推荐响应时间
        long start = System.currentTimeMillis();

        // 构建召回上下文，包含用户ID、页码和每页大小等信息，供召回策略使用
        RecallContext context = RecallContext.builder()
                .userId(userId)
                .pageNum(pageQuery.getPageNum())
                .pageSize(pageQuery.getPageSize())
                .build();

        // 执行多种召回策略，收集召回候选结果，供后续排序和过滤使用
        List<RecallCandidate> candidates = new ArrayList<>();
        for (RecallStrategy strategy : recallStrategies) {
            candidates.addAll(strategy.recall(context));
        }

        // 计算期望的推荐结果数量，通常为页码乘以每页大小，以确保排序后能够返回足够的结果进行分页展示
        int expected = pageQuery.getPageNum() * pageQuery.getPageSize();

        // 对召回候选结果进行排序，得到最终的推荐结果列表，排序过程中可以结合多种特征和算法进行综合评分，并且可以根据需要过滤掉一些不合适的结果
        List<RankedRecommend> ranked = recommendRankService.rank(candidates, null, Math.max(expected, pageQuery.getPageSize()));

        // 持久化推荐结果并构建返回给前端的VO对象列表，记录推荐请求的相关信息和推荐结果的详细数据，以便后续分析和优化推荐算法
        List<RecommendItemVO> pageRecords = persistAndBuildResult(userId, RecommendType.HOME, "home", ranked, pageQuery, System.currentTimeMillis() - start);

        // 计算总记录数和总页数等分页信息，构建分页结果对象，供前端展示使用
        long total = ranked.size();
        long totalPage = total == 0 ? 0 : (total + pageQuery.getPageSize() - 1) / pageQuery.getPageSize();
        PageResult<RecommendItemVO> pageResult = PageResult.<RecommendItemVO>builder()
                .records(pageRecords)
                .total(total)
                .pageNum(pageQuery.getPageNum())
                .pageSize(pageQuery.getPageSize())
                .totalPage(totalPage)
                .build();

        // 将推荐结果缓存到Redis中，设置适当的过期时间，以便后续相同请求能够直接从缓存中获取结果，提高响应速度和系统性能
        RecommendCachePayload payload = new RecommendCachePayload();
        payload.setRecords(pageRecords);
        payload.setTotal(total);
        payload.setTotalPage(totalPage);
        redisUtils.set(cacheKey, JsonUtils.toJson(payload), HOME_CACHE_TTL);
        return pageResult;
    }

    /**
     * 景点相似推荐接口，基于目标景点的特征和标签等信息，结合多种召回策略进行推荐，并对结果进行排序和分页展示。
     *
     * @param scenicId  景点ID，表示需要获取相似推荐的目标景点，通过该ID进行相似度计算和推荐生成
     * @param pageQuery 分页查询参数，包含页码和每页大小等信息，用于控制返回结果的分页展示
     * @return 分页结果，包含推荐结果列表、总记录数、当前页码、每页大小和总页数等信息，供前端展示使用
     */
    @Override
    public PageResult<RecommendItemVO> scenicSimilarRecommend(Long scenicId, PageQuery pageQuery) {
        Long userId = getCurrentUserIdRequired();
        // 构建缓存键，格式为 "recommend:scenic:{scenicId}:similar:{pageNum}:{pageSize}"，用于在Redis中存储和查询景点相似推荐结果的缓存
        String cacheKey = "recommend:scenic:" + scenicId + ":similar:" + pageQuery.getPageNum() + ":" + pageQuery.getPageSize();
        // 尝试从Redis缓存中获取推荐结果，如果存在且不为空，则直接返回缓存中的结果，避免重复计算和数据库查询，提高响应速度
        String cacheValue = redisUtils.get(cacheKey);
        if (cacheValue != null && !cacheValue.isBlank()) {
            RecommendCachePayload payload = JsonUtils.fromJson(cacheValue, RecommendCachePayload.class);
            return PageResult.<RecommendItemVO>builder()
                    .records(payload.getRecords() == null ? Collections.emptyList() : payload.getRecords())
                    .total(payload.getTotal())
                    .pageNum(pageQuery.getPageNum())
                    .pageSize(pageQuery.getPageSize())
                    .totalPage(payload.getTotalPage())
                    .build();
        }

        // 记录推荐请求的开始时间，用于后续计算推荐响应时间
        long start = System.currentTimeMillis();

        // 构建召回上下文，包含用户ID、目标景点ID、页码和每页大小等信息，供召回策略使用
        RecallContext context = RecallContext.builder()
                .userId(userId)
                .scenicId(scenicId)
                .pageNum(pageQuery.getPageNum())
                .pageSize(pageQuery.getPageSize())
                .build();

        // 执行多种召回策略，收集召回候选结果，供后续排序和过滤使用
        List<RecallCandidate> candidates = new ArrayList<>();
        for (RecallStrategy strategy : recallStrategies) {
            // 如果是标签召回策略，则跳过，因为相似推荐的标签召回通过 recallByCurrentScenicTags 方法单独处理，避免与基于用户行为的标签召回策略叠加造成重复召回
            if (TAG_STRATEGY_NAME.equals(strategy.strategyName())) {
                continue;
            }
            candidates.addAll(strategy.recall(context));
        }
        // 通过当前景点的标签进行相似景点的召回，获取与目标景点具有相似标签的其他景点作为候选结果，补充基于标签的相似推荐，增强推荐结果的相关性和多样性
        candidates.addAll(recallByCurrentScenicTags(scenicId));

        // 计算期望的推荐结果数量，通常为页码乘以每页大小，以确保排序后能够返回足够的结果进行分页展示
        int expected = pageQuery.getPageNum() * pageQuery.getPageSize();

        // 对召回候选结果进行排序，得到最终的推荐结果列表，排序过程中可以结合多种特征和算法进行综合评分，并且可以根据需要过滤掉一些不合适的结果
        List<RankedRecommend> ranked = recommendRankService.rank(candidates, scenicId, Math.max(expected, pageQuery.getPageSize()));

        // 持久化推荐结果并构建返回给前端的VO对象列表，记录推荐请求的相关信息和推荐结果的详细数据，以便后续分析和优化推荐算法
        List<RecommendItemVO> pageRecords = persistAndBuildResult(userId, RecommendType.SIMILAR, "scenic-similar", ranked, pageQuery, System.currentTimeMillis() - start);

        // 计算总记录数和总页数等分页信息，构建分页结果对象，供前端展示使用
        long total = ranked.size();
        long totalPage = total == 0 ? 0 : (total + pageQuery.getPageSize() - 1) / pageQuery.getPageSize();
        // 构建分页结果对象，包含推荐结果列表、总记录数、当前页码、每页大小和总页数等信息，供前端展示使用
        PageResult<RecommendItemVO> pageResult = PageResult.<RecommendItemVO>builder()
                .records(pageRecords)
                .total(total)
                .pageNum(pageQuery.getPageNum())
                .pageSize(pageQuery.getPageSize())
                .totalPage(totalPage)
                .build();

        // 将推荐结果缓存到Redis中，设置适当的过期时间，以便后续相同请求能够直接从缓存中获取结果，提高响应速度和系统性能
        RecommendCachePayload payload = new RecommendCachePayload();
        payload.setRecords(pageRecords);
        payload.setTotal(total);
        payload.setTotalPage(totalPage);
        redisUtils.set(cacheKey, JsonUtils.toJson(payload), SIMILAR_CACHE_TTL);
        return pageResult;
    }


    /**
     * 持久化推荐结果并构建返回给前端的VO对象列表，记录推荐请求的相关信息和推荐结果的详细数据，以便后续分析和优化推荐算法。
     *
     * @param userId         当前用户ID，表示进行推荐请求的用户，用于关联推荐记录和结果项的用户信息
     * @param recommendType  推荐类型，1表示首页推荐，2表示景点相似推荐，用于区分不同场景下的推荐请求和结果
     * @param scene          推荐场景，描述推荐发生的具体场景和上下文信息，供后续分析和优化使用
     * @param ranked         推荐结果列表，包含经过排序的推荐项数据，供构建返回结果和记录推荐结果使用
     * @param pageQuery      分页查询参数，包含页码和每页大小等信息，用于控制返回结果的分页展示和记录推荐请求的相关参数
     * @param responseTimeMs 推荐响应时间，单位为毫秒，用于记录推荐请求的性能指标和优化推荐算法使用
     * @return 推荐结果的VO对象列表，包含推荐记录ID、结果项ID、景点ID、景点名称、封面图片、评分、推荐理由、来源类型和排序分数等信息，供前端展示使用
     */
    private List<RecommendItemVO> persistAndBuildResult(Long userId,
                                                        RecommendType recommendType,
                                                        String scene,
                                                        List<RankedRecommend> ranked,
                                                        PageQuery pageQuery,
                                                        long responseTimeMs) {
        // 计算分页的起始和结束索引，确保不会越界，如果起始索引大于等于结束索引，则返回空列表
        long offset = (long) (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int fromIndex = (int) Math.min(offset, ranked.size());
        int toIndex = Math.min(fromIndex + pageQuery.getPageSize(), ranked.size());
        if (fromIndex >= toIndex) {
            return Collections.emptyList();
        }

        // 截取当前页的推荐结果列表，供后续构建返回结果和记录推荐结果使用
        List<RankedRecommend> pageItems = ranked.subList(fromIndex, toIndex);

        // 创建推荐记录对象，记录推荐请求的相关信息，包括用户ID、推荐类型、推荐场景、请求参数、使用的算法、LLM调用次数、候选总数、返回数量和响应时间等信息，并将其插入到数据库中
        RecommendRecord record = new RecommendRecord();
        record.setUserId(userId);
        record.setRecommendType(recommendType.getCode());
        record.setScene(scene);
        record.setRequestParams(JsonUtils.toJson(pageQuery));
        record.setAlgorithm("规则召回_V1");
        record.setLlmUsed(0);
        record.setTotalCandidates(ranked.size());
        record.setReturnedCount(pageItems.size());
        record.setResponseTimeMs((int) responseTimeMs);
        // 插入推荐记录到数据库中，生成推荐记录ID，供后续关联推荐结果项使用
        recommendRecordMapper.insert(record);

        // 创建最终返回给前端的推荐结果项VO对象列表，记录推荐结果的详细数据，包括推荐记录ID、景点ID、排名位置、排序分数、推荐理由、点击和收藏状态等信息
        List<RecommendItemVO> vos = new ArrayList<>();
        for (int i = 0; i < pageItems.size(); i++) {
            RankedRecommend rankedRecommend = pageItems.get(i);

            // 创建推荐结果项对象，记录推荐结果的详细数据，包括推荐记录ID、景点ID、排名位置、排序分数、推荐理由、点击和收藏状态等信息，并将其插入到数据库中，生成推荐结果项ID，供后续关联反馈数据使用
            RecommendResultItem item = new RecommendResultItem();
            item.setRecommendRecordId(record.getId());
            item.setScenicSpotId(rankedRecommend.getScenicSpot().getId());
            item.setRankPosition(fromIndex + i + 1);
            item.setScore(rankedRecommend.getRankScore());
            item.setReason(recommendReasonBuilder.build(rankedRecommend.getSourceTypes()));
            item.setIsClicked(0);
            item.setIsFavorited(0);
            // 插入推荐结果项到数据库中，生成推荐结果项ID，供后续关联反馈数据使用
            recommendResultItemMapper.insert(item);

            // 构建返回给前端的VO对象，包含推荐记录ID、结果项ID、景点ID、景点名称、封面图片、评分、推荐理由、来源类型和排序分数等信息，供前端展示使用
            RecommendItemVO vo = new RecommendItemVO();
            vo.setRecommendRecordId(record.getId());
            vo.setResultItemId(item.getId());
            vo.setScenicId(rankedRecommend.getScenicSpot().getId());
            vo.setScenicName(rankedRecommend.getScenicSpot().getName());
            vo.setCoverImage(rankedRecommend.getScenicSpot().getCoverImage());
            vo.setScore(rankedRecommend.getScenicSpot().getScore() == null
                    ? BigDecimal.ZERO : rankedRecommend.getScenicSpot().getScore());
            vo.setRecommendReason(item.getReason());
            vo.setSourceType(String.join(",", rankedRecommend.getSourceTypes()));
            vo.setRankScore(rankedRecommend.getRankScore());
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 通过当前景点的标签进行相似景点的召回，获取与目标景点具有相似标签的其他景点作为候选结果，补充基于标签的相似推荐，增强推荐结果的相关性和多样性。
     *
     * @param scenicId 目标景点ID，通过该ID查询其关联的标签，并找到具有相似标签的其他景点作为候选结果，供后续排序和过滤使用
     * @return 召回候选结果列表，包含与目标景点具有相似标签的其他景点的ID、来源类型和基础分数等信息，供后续排序和过滤使用
     */
    private List<RecallCandidate> recallByCurrentScenicTags(Long scenicId) {
        // 查询当前景点关联的标签列表，如果没有关联标签，则返回空列表，避免后续查询和计算
        List<ScenicSpotTag> seedTags = scenicSpotTagMapper.selectList(new LambdaQueryWrapper<ScenicSpotTag>()
                .eq(ScenicSpotTag::getScenicSpotId, scenicId));
        if (seedTags.isEmpty()) {
            return Collections.emptyList();
        }
        // 提取标签ID列表，供后续查询具有相似标签的其他景点使用
        Set<Long> tagIds = new HashSet<>();
        seedTags.forEach(item -> tagIds.add(item.getTagId()));

        // 查询具有相似标签的其他景点列表，限制查询数量以控制性能，避免查询过多数据导致性能问题
        List<ScenicSpotTag> similar = scenicSpotTagMapper.selectList(new LambdaQueryWrapper<ScenicSpotTag>()
                .in(ScenicSpotTag::getTagId, tagIds)
                .last("limit 200"));

        // 构建召回候选结果列表，包含与目标景点具有相似标签的其他景点的ID、来源类型和基础分数等信息，供后续排序和过滤使用
        return similar.stream()
                .map(ScenicSpotTag::getScenicSpotId)
                // 过滤掉与目标景点相同的景点ID，避免推荐结果中出现目标景点本身
                .filter(id -> !id.equals(scenicId))
                .distinct()
                .map(id -> RecallCandidate.builder()
                        .scenicId(id)
                        .sourceType("SIMILAR_TAG")
                        .baseScore(BigDecimal.valueOf(1.2D))
                        .build())
                .toList();
    }


    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * RecommendCachePayload是一个用于缓存推荐结果的静态内部类，包含推荐结果列表、总记录数和总页数等信息，供Redis缓存使用。
     * 该类使用了@Data注解，自动生成了getter、setter、toString、equals和hashCode等常用方法。
     */
    @Data
    private static class RecommendCachePayload {

        private List<RecommendItemVO> records;

        private Long total;

        private Long totalPage;
    }
}
