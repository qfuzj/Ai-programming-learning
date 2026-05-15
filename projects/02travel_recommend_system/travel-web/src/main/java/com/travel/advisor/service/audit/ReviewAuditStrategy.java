package com.travel.advisor.service.audit;

import com.travel.advisor.common.enums.UserReviewStatus;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.entity.UserReview;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 点评审核策略：处理 review 类型的审核，更新 user_review 状态并刷新景点评分。
 */
@Component("reviewAuditStrategy")
@RequiredArgsConstructor
public class ReviewAuditStrategy implements AuditStrategy {

    private final UserReviewMapper userReviewMapper;
    private final ScenicSpotMapper scenicSpotMapper;

    @Override
    public void executeAudit(Long contentId, String action, AuditActionDTO dto) {
        UserReview userReview = userReviewMapper.selectById(contentId);
        if (userReview == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "点评不存在");
        }
        UserReview updateReview = new UserReview();
        updateReview.setId(userReview.getId());
        updateReview.setStatus(resolveContentStatus(action));
        updateReview.setAuditRemark(dto == null ? null : dto.getReason());
        updateReview.setUpdateTime(LocalDateTime.now());
        userReviewMapper.updateById(updateReview);
    }

    @Override
    public void refreshRelatedData(Long contentId) {
        // 刷新景点评分和评价人数
        UserReview userReview = userReviewMapper.selectById(contentId);
        if (userReview == null) {
            return;
        }
        Long scenicSpotId = userReview.getScenicSpotId();
        Double averageRating = userReviewMapper.selectAverageRatingByScenicSpotId(scenicSpotId);
        Integer ratingCount = userReviewMapper.countByScenicSpotId(scenicSpotId);
        scenicSpotMapper.updateScoreAndRatingCount(
                scenicSpotId,
                Objects.requireNonNullElse(averageRating, 0D),
                Objects.requireNonNullElse(ratingCount, 0)
        );
    }

    private Integer resolveContentStatus(String action) {
        return switch (action) {
            case "approve" -> UserReviewStatus.APPROVED.getCode();
            case "reject" -> UserReviewStatus.REJECTED.getCode();
            case "hide" -> UserReviewStatus.HIDDEN.getCode();
            default -> throw new IllegalArgumentException("未知的操作类型: " + action);
        };
    }
}
