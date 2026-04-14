package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.travel.advisor.dto.user.UserPreferenceTagsUpdateDTO;
import com.travel.advisor.dto.user.UserProfileUpdateDTO;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.entity.User;
import com.travel.advisor.entity.UserBrowseHistory;
import com.travel.advisor.entity.UserFavorite;
import com.travel.advisor.entity.UserPreferenceTag;
import com.travel.advisor.entity.UserProfile;
import com.travel.advisor.entity.UserReview;
import com.travel.advisor.entity.ScenicSpotTag;
import com.travel.advisor.mapper.ScenicSpotTagMapper;
import com.travel.advisor.mapper.TagMapper;
import com.travel.advisor.mapper.UserBrowseHistoryMapper;
import com.travel.advisor.mapper.UserFavoriteMapper;
import com.travel.advisor.mapper.UserMapper;
import com.travel.advisor.mapper.UserPreferenceTagMapper;
import com.travel.advisor.mapper.UserProfileMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import com.travel.advisor.security.LoginUser;
import com.travel.advisor.service.UserProfileService;
import com.travel.advisor.utils.BeanCopyUtils;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.user.UserProfilePortraitVO;
import com.travel.advisor.vo.user.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserPreferenceTagMapper userPreferenceTagMapper;
    private final TagMapper tagMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserReviewMapper userReviewMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;

    private static final int RECENT_BROWSE_LIMIT = 30;
    private static final int RECENT_PREFERENCES_TOP_N = 5;
    private static final double BROWSE_WEIGHT = 1D;
    private static final double FAVORITE_WEIGHT = 3D;
    private static final double REVIEW_WEIGHT = 5D;

    @Override
    public UserProfileVO getMyProfile() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        User user = userMapper.selectById(loginUser.getUserId());
        UserProfileVO vo = BeanCopyUtils.copy(user, UserProfileVO.class);
        vo.setRole("USER");
        return vo;
    }

    @Override
    public void updateMyProfile(UserProfileUpdateDTO dto) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setGender(dto.getGender());
        user.setBirthday(dto.getBirthday());
        user.setSignature(dto.getSignature());
        userMapper.updateById(user);
    }

    @Override
    public UserProfilePortraitVO getMyPortrait() {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        UserProfilePortraitVO vo = new UserProfilePortraitVO();
        
        // 1. 获取 user_profile 核心画像
        UserProfile profile = userProfileMapper.selectOne(Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
        if (profile != null) {
            vo.setTravelStyle(profile.getTravelStyle() != null ? profile.getTravelStyle() : "待发掘");
            Integer budget = profile.getBudgetLevel();
            vo.setBudgetLevel(budget != null ? (budget == 1 ? "经济型" : (budget == 2 ? "舒适型" : "奢华型")) : "未知");
            vo.setSummary("基于足迹与偏好综合生成的旅行摘要");
        } else {
            vo.setTravelStyle("待发掘");
            vo.setBudgetLevel("未知");
            vo.setSummary("暂无足够数据生成画像");
        }
        
        // 2. 获取用户偏好标签名列表
        List<UserPreferenceTag> prefTags = userPreferenceTagMapper.selectList(Wrappers.<UserPreferenceTag>lambdaQuery().eq(UserPreferenceTag::getUserId, userId));
        if (!prefTags.isEmpty()) {
            List<Long> tagIds = prefTags.stream().map(UserPreferenceTag::getTagId).collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            vo.setPreferredTags(tags.stream().map(Tag::getName).collect(Collectors.toList()));
        } else {
            vo.setPreferredTags(new ArrayList<>());
        }
        
        vo.setRecentPreferences(buildRecentPreferences(userId));
        vo.setLocation("未知地区");
        return vo;
    }

    private List<String> buildRecentPreferences(Long userId) {
        // 1) 浏览：仅取最近30条，按景点聚合次数
        List<UserBrowseHistory> recentBrowseList = userBrowseHistoryMapper.selectList(
                Wrappers.<UserBrowseHistory>lambdaQuery()
                        .eq(UserBrowseHistory::getUserId, userId)
                        .isNotNull(UserBrowseHistory::getScenicSpotId)
                        .orderByDesc(UserBrowseHistory::getCreateTime)
                        .orderByDesc(UserBrowseHistory::getId)
                        .last("LIMIT " + RECENT_BROWSE_LIMIT)
        );

        Map<Long, Long> scenicBrowseCountMap = recentBrowseList.stream()
                .collect(Collectors.groupingBy(UserBrowseHistory::getScenicSpotId, Collectors.counting()));

        // 2) 收藏：按景点聚合次数（同景点多收藏夹也能正确累加）
        List<UserFavorite> favoriteList = userFavoriteMapper.selectList(
                Wrappers.<UserFavorite>lambdaQuery()
                        .eq(UserFavorite::getUserId, userId)
                        .isNotNull(UserFavorite::getScenicSpotId)
        );
        Map<Long, Long> scenicFavoriteCountMap = favoriteList.stream()
                .collect(Collectors.groupingBy(UserFavorite::getScenicSpotId, Collectors.counting()));

        // 3) 评论：按景点聚合次数
        List<UserReview> reviewList = userReviewMapper.selectList(
                Wrappers.<UserReview>lambdaQuery()
                        .eq(UserReview::getUserId, userId)
                        .isNotNull(UserReview::getScenicSpotId)
        );
        Map<Long, Long> scenicReviewCountMap = reviewList.stream()
                .collect(Collectors.groupingBy(UserReview::getScenicSpotId, Collectors.counting()));

        // 4) 先聚合到景点分：scenicScore = browse*1 + favorite*3 + review*5
        Map<Long, Double> scenicScoreMap = new HashMap<>();
        scenicBrowseCountMap.forEach((scenicId, count) -> scenicScoreMap.merge(scenicId, count * BROWSE_WEIGHT, Double::sum));
        scenicFavoriteCountMap.forEach((scenicId, count) -> scenicScoreMap.merge(scenicId, count * FAVORITE_WEIGHT, Double::sum));
        scenicReviewCountMap.forEach((scenicId, count) -> scenicScoreMap.merge(scenicId, count * REVIEW_WEIGHT, Double::sum));

        if (scenicScoreMap.isEmpty()) {
            return new ArrayList<>();
        }

        // 5) 批量查询景点-标签关系，避免 N+1
        List<Long> scenicIds = new ArrayList<>(scenicScoreMap.keySet());
        List<ScenicSpotTag> scenicSpotTags = scenicSpotTagMapper.selectByScenicSpotIds(scenicIds);
        if (scenicSpotTags == null || scenicSpotTags.isEmpty()) {
            return new ArrayList<>();
        }

        // 6) 按标签聚合最终分值：tagScore = ∑ scenicScore
        Map<Long, Double> tagScoreMap = new HashMap<>();
        for (ScenicSpotTag scenicSpotTag : scenicSpotTags) {
            Long scenicId = scenicSpotTag.getScenicSpotId();
            Long tagId = scenicSpotTag.getTagId();
            if (scenicId == null || tagId == null) {
                continue;
            }
            Double scenicScore = scenicScoreMap.getOrDefault(scenicId, 0D);
            if (scenicScore <= 0D) {
                continue;
            }
            tagScoreMap.merge(tagId, scenicScore, Double::sum);
        }

        if (tagScoreMap.isEmpty()) {
            return new ArrayList<>();
        }

        // 7) 取标签 Top5（分值降序）
        List<Long> topTagIds = tagScoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(RECENT_PREFERENCES_TOP_N)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (topTagIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 8) 批量查标签名并保持 Top 排序
        List<Tag> tags = tagMapper.selectBatchIds(topTagIds);
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, String> tagNameMap = tags.stream()
                .filter(tag -> tag.getId() != null && tag.getName() != null && !tag.getName().isBlank())
                .collect(Collectors.toMap(Tag::getId, Tag::getName, (a, b) -> a));

        return topTagIds.stream()
                .map(tagNameMap::get)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toList());
    }

    @Override
    public List<Tag> getMyPreferenceTags() {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        List<UserPreferenceTag> prefTags = userPreferenceTagMapper.selectList(Wrappers.<UserPreferenceTag>lambdaQuery().eq(UserPreferenceTag::getUserId, userId));
        if (prefTags.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> tagIds = prefTags.stream().map(UserPreferenceTag::getTagId).collect(Collectors.toList());
        return tagMapper.selectBatchIds(tagIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePreferenceTags(UserPreferenceTagsUpdateDTO dto) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        // 1. 删除旧的偏好标签
        userPreferenceTagMapper.delete(Wrappers.<UserPreferenceTag>lambdaQuery().eq(UserPreferenceTag::getUserId, userId));
        // 2. 插入新的偏好标签
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                UserPreferenceTag tag = new UserPreferenceTag();
                tag.setUserId(userId);
                tag.setTagId(tagId);
                userPreferenceTagMapper.insert(tag);
            }
        }
    }
}
