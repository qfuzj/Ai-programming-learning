package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.recommend.RecommendFeedbackDTO;
import com.travel.advisor.entity.RecommendRecord;
import com.travel.advisor.entity.RecommendResultItem;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.RecommendRecordMapper;
import com.travel.advisor.mapper.RecommendResultItemMapper;
import com.travel.advisor.service.RecommendFeedbackService;
import com.travel.advisor.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 推荐反馈服务实现类，独立处理推荐反馈操作。
 * 该类仅实现 RecommendFeedbackService 接口，职责为反馈处理，与推荐查询服务解耦。
 */
@Service
@RequiredArgsConstructor
public class RecommendFeedbackServiceImpl implements RecommendFeedbackService {

    private final RecommendRecordMapper recommendRecordMapper;
    private final RecommendResultItemMapper recommendResultItemMapper;

    /**
     * 记录推荐曝光事件(用户看到了推荐结果，但没有点击)，仅校验曝光的有效性，不更新结果项状态。
     *
     * @param dto 推荐反馈DTO，包含推荐记录ID、结果项ID和景点ID
     */
    @Override
    public void exposure(RecommendFeedbackDTO dto) {
        validateFeedbackOwnership(dto);
    }

    /**
     * 记录用户点开了推荐项，更新结果项的点击状态和点击时间。
     *
     * @param dto 推荐反馈DTO，包含推荐记录ID、结果项ID和景点ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void click(RecommendFeedbackDTO dto) {
        RecommendResultItem item = validateFeedbackOwnership(dto);
        RecommendResultItem update = new RecommendResultItem();
        update.setId(item.getId());
        update.setIsClicked(1);
        update.setClickTime(LocalDateTime.now());
        recommendResultItemMapper.updateById(update);
    }

    /**
     * 记录用户收藏了推荐项，更新结果项的收藏状态。
     *
     * @param dto 推荐反馈DTO，包含推荐记录ID、结果项ID和景点ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void favorite(RecommendFeedbackDTO dto) {
        RecommendResultItem item = validateFeedbackOwnership(dto);
        RecommendResultItem update = new RecommendResultItem();
        update.setId(item.getId());
        update.setIsFavorited(1);
        recommendResultItemMapper.updateById(update);
    }

    /**
     * 校验推荐反馈的所有权，确保当前用户对该记录和结果项具有操作权限。
     *
     * @param dto 推荐反馈DTO
     * @return 推荐结果项对象
     * @throws BusinessException 如果校验不通过
     */
    private RecommendResultItem validateFeedbackOwnership(RecommendFeedbackDTO dto) {
        Long userId = getCurrentUserIdRequired();

        // 校验推荐记录属于当前用户
        RecommendRecord record = recommendRecordMapper.selectOne(new LambdaQueryWrapper<RecommendRecord>()
                .eq(RecommendRecord::getId, dto.getRecommendRecordId())
                .eq(RecommendRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "推荐记录不存在");
        }

        // 校验结果项属于该推荐记录
        RecommendResultItem item = recommendResultItemMapper.selectOne(new LambdaQueryWrapper<RecommendResultItem>()
                .eq(RecommendResultItem::getId, dto.getResultItemId())
                .eq(RecommendResultItem::getRecommendRecordId, dto.getRecommendRecordId())
                .eq(RecommendResultItem::getScenicSpotId, dto.getScenicId()));
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "推荐结果项不存在");
        }
        return item;
    }

    /**
     * 获取当前登录用户的ID，确保用户已认证。
     *
     * @return 当前用户ID
     * @throws BusinessException 如果用户未认证
     */
    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
