package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.review.ReviewCreateDTO;
import com.travel.advisor.entity.ContentAudit;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.entity.User;
import com.travel.advisor.entity.UserReview;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.ContentAuditMapper;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.UserMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import com.travel.advisor.service.ReviewService;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.review.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final UserReviewMapper userReviewMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final UserMapper userMapper;
    private final ContentAuditMapper contentAuditMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long create(ReviewCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(dto.getScenicId());
        if (scenicSpot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "景点不存在");
        }

        UserReview review = new UserReview();
        review.setUserId(userId);
        review.setScenicSpotId(dto.getScenicId());
        review.setRating(dto.getScore());
        review.setContent(dto.getContent());
        review.setImages(dto.getImageIds() == null ? null : JsonUtils.toJson(dto.getImageIds()));
        review.setVisitDate(dto.getVisitDate());
        review.setTravelType(dto.getTravelType());
        review.setLikeCount(0);
        review.setReplyCount(0);
        review.setIsAnonymous(dto.getIsAnonymous() == null ? 0 : dto.getIsAnonymous());
        review.setStatus(0);
        userReviewMapper.insert(review);

        ContentAudit audit = new ContentAudit();
        audit.setContentType("review");
        audit.setContentId(review.getId());
        audit.setSubmitUserId(userId);
        audit.setAuditStatus(0);
        audit.setContentSnapshot(JsonUtils.toJson(dto));
        contentAuditMapper.insert(audit);

        return review.getId();
    }

    @Override
    public PageResult<ReviewVO> pageMyReviews(PageQuery pageQuery) {
        Long userId = getCurrentUserIdRequired();
        Page<UserReview> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<UserReview> result = userReviewMapper.selectPage(page, new LambdaQueryWrapper<UserReview>()
            .eq(UserReview::getUserId, userId)
            .orderByDesc(UserReview::getCreateTime));

        List<ReviewVO> records = buildReviewVO(result.getRecords());
        return PageResult.<ReviewVO>builder()
            .records(records)
            .total(result.getTotal())
            .pageNum(Math.toIntExact(result.getCurrent()))
            .pageSize(Math.toIntExact(result.getSize()))
            .totalPage(result.getPages())
            .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteMyReview(Long id) {
        Long userId = getCurrentUserIdRequired();
        int affected = userReviewMapper.delete(new LambdaQueryWrapper<UserReview>()
            .eq(UserReview::getId, id)
            .eq(UserReview::getUserId, userId));
        if (affected == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "点评不存在");
        }
    }

    /**
     * 分页查询景点点评列表
     * @param scenicId 景点ID
     * @param pageQuery 分页查询参数
     * @return 点评列表分页结果
     */
    @Override
    public PageResult<ReviewVO> pageScenicReviews(Long scenicId, PageQuery pageQuery) {
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(scenicId);
        if (scenicSpot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "景点不存在");
        }

        LambdaQueryWrapper<UserReview> wrapper = new LambdaQueryWrapper<UserReview>()
            .eq(UserReview::getScenicSpotId, scenicId)
            .eq(UserReview::getStatus, 1);
        applySort(wrapper, pageQuery.getSortBy());

        Page<UserReview> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<UserReview> result = userReviewMapper.selectPage(page, wrapper);

        List<ReviewVO> records = buildReviewVO(result.getRecords());
        return PageResult.<ReviewVO>builder()
            .records(records)
            .total(result.getTotal())
            .pageNum(Math.toIntExact(result.getCurrent()))
            .pageSize(Math.toIntExact(result.getSize()))
            .totalPage(result.getPages())
            .build();
    }

    /**
     * 构建点评VO列表
     * @param reviews 某一个景点的所有用户点评列表
     * @return 点评VO列表
     */
    private List<ReviewVO> buildReviewVO(List<UserReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> scenicIds = reviews.stream().map(UserReview::getScenicSpotId).distinct().toList();
        Map<Long, ScenicSpot> scenicMap = scenicSpotMapper.selectBatchIds(scenicIds).stream()
            .collect(Collectors.toMap(ScenicSpot::getId, item -> item));

        List<Long> userIds = reviews.stream().map(UserReview::getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, item -> item));

        return reviews.stream().map(item -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(item.getId());
            vo.setUserId(item.getUserId());
            User user = userMap.get(item.getUserId());
            vo.setUsername(user == null ? "" : user.getUsername());
            vo.setScenicId(item.getScenicSpotId());
            ScenicSpot scenicSpot = scenicMap.get(item.getScenicSpotId());
            vo.setScenicName(scenicSpot == null ? "" : scenicSpot.getName());
            vo.setScore(item.getRating());
            vo.setContent(item.getContent());
            vo.setImageIds(parseImageIds(item.getImages()));
            vo.setVisitDate(item.getVisitDate());
            vo.setTravelType(item.getTravelType());
            vo.setLikeCount(item.getLikeCount());
            vo.setReplyCount(item.getReplyCount());
            vo.setIsAnonymous(item.getIsAnonymous());
            vo.setStatus(item.getStatus());
            vo.setAuditRemark(item.getAuditRemark());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).toList();
    }

    private List<Long> parseImageIds(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Long[] arr = JsonUtils.fromJson(images, Long[].class);
            return arr == null ? Collections.emptyList() : List.of(arr);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void applySort(LambdaQueryWrapper<UserReview> wrapper, String sortBy) {
        if ("scoreDesc".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(UserReview::getRating).orderByDesc(UserReview::getCreateTime);
            return;
        }
        if ("scoreAsc".equalsIgnoreCase(sortBy)) {
            wrapper.orderByAsc(UserReview::getRating).orderByDesc(UserReview::getCreateTime);
            return;
        }
        wrapper.orderByDesc(UserReview::getCreateTime);
    }

    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
