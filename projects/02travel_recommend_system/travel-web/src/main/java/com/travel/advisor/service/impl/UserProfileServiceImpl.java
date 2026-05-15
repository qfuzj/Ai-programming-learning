package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.travel.advisor.common.enums.BizType;
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
import org.springframework.util.StringUtils;
import com.travel.advisor.mapper.TagMapper;
import com.travel.advisor.mapper.UserBrowseHistoryMapper;
import com.travel.advisor.mapper.UserFavoriteMapper;
import com.travel.advisor.mapper.UserMapper;
import com.travel.advisor.mapper.UserPreferenceTagMapper;
import com.travel.advisor.mapper.UserProfileMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import com.travel.advisor.security.LoginUser;
import com.travel.advisor.service.FileService;
import com.travel.advisor.service.UserProfileService;
import com.travel.advisor.service.portrait.PortraitSummaryContext;
import com.travel.advisor.service.portrait.UserPortraitAnalyzer;
import com.travel.advisor.utils.BeanCopyUtils;
import com.travel.advisor.utils.FileResourceIds;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.user.UserProfilePortraitVO;
import com.travel.advisor.vo.user.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private final com.travel.advisor.mapper.RegionMapper regionMapper;
    private final UserPreferenceTagMapper userPreferenceTagMapper;
    private final TagMapper tagMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserReviewMapper userReviewMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;
    private final FileService fileService;
    private final UserPortraitAnalyzer userPortraitAnalyzer;

    private static final int RECENT_BROWSE_LIMIT = 30;
    private static final int RECENT_PREFERENCES_TOP_N = 5;
    private static final double BROWSE_WEIGHT = 1D;
    private static final double FAVORITE_WEIGHT = 3D;
    private static final double REVIEW_WEIGHT = 5D;
    /** 画像数据过期阈值：超过此时长则触发重新分析。 */
    private static final Duration PORTRAIT_REFRESH_INTERVAL = Duration.ofHours(6);

    private String resolveAvatarUrl(String avatar) {
        if (!StringUtils.hasText(avatar)) {
            return "";
        }
        Long fileId = FileResourceIds.tryParseId(avatar);
        if (fileId == null) {
            return avatar.trim(); // 旧数据直接返回原 URL
        }
        return fileService.resolveUrls(List.of(fileId)).getOrDefault(fileId, "");
    }

    /**
     * 获取我的个人信息，包括基本信息和角色信息，以及解析头像 URL 和地区名称。
     */
    @Override
    public UserProfileVO getMyProfile() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        User user = userMapper.selectById(loginUser.getUserId());
        UserProfileVO vo = BeanCopyUtils.copy(user, UserProfileVO.class);
        vo.setRole("USER");
        vo.setAvatar(resolveAvatarUrl(user.getAvatar()));
        vo.setRegionId(user.getRegionId());
        if (user.getRegionId() != null) {
            try {
                var region = regionMapper.selectById(user.getRegionId());
                vo.setRegionName(region == null ? null : region.getName());
            } catch (Exception ignored) {
                // ignore
            }
        }
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
        user.setRegionId(dto.getRegionId());
        user.setBirthday(dto.getBirthday());
        user.setSignature(dto.getSignature());
        userMapper.updateById(user);
        Long avatarFileId = FileResourceIds.tryParseId(dto.getAvatar());
        if (avatarFileId != null) {
            fileService.bindFilesToBiz(List.of(avatarFileId), userId, BizType.AVATAR);
        }
        // 旧 URL 格式，不绑定
    }

    @Override
    public UserProfilePortraitVO getMyPortrait() {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return getPortraitByUserId(userId);
    }

    @Override
    public UserProfilePortraitVO getPortraitByUserId(Long userId) {
        UserProfilePortraitVO vo = new UserProfilePortraitVO();

        UserProfile profile = userProfileMapper
                .selectOne(Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
        if (shouldRefreshPortrait(profile)) {
            try {
                userPortraitAnalyzer.analyzeAndPersist(userId);
                profile = userProfileMapper
                        .selectOne(Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
            } catch (Exception ignored) {
                // 分析失败时降级为旧数据/默认值，不影响接口返回
            }
        }

        String travelStyle = profile != null && profile.getTravelStyle() != null && !profile.getTravelStyle().isBlank()
                ? profile.getTravelStyle()
                : "待发掘";
        String budgetLabel = mapBudgetLevel(profile == null ? null : profile.getBudgetLevel());
        vo.setTravelStyle(travelStyle);
        vo.setBudgetLevel(budgetLabel);

        List<UserPreferenceTag> prefTags = userPreferenceTagMapper
                .selectList(Wrappers.<UserPreferenceTag>lambdaQuery().eq(UserPreferenceTag::getUserId, userId));
        List<String> preferredTagNames;
        if (!prefTags.isEmpty()) {
            List<Long> tagIds = prefTags.stream().map(UserPreferenceTag::getTagId).collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            preferredTagNames = tags.stream().map(Tag::getName).collect(Collectors.toList());
        } else {
            preferredTagNames = new ArrayList<>();
        }
        vo.setPreferredTags(preferredTagNames);

        List<String> recentPreferences = buildRecentPreferences(userId);
        vo.setRecentPreferences(recentPreferences);

        String location = resolveUserLocation(userId);
        vo.setLocation(location);

        String preferredSeason = profile == null ? null : profile.getPreferredSeason();
        vo.setSummary(buildSummary(userId, profile, travelStyle, budgetLabel, preferredSeason,
                preferredTagNames, recentPreferences, location));

        return vo;
    }

    private boolean shouldRefreshPortrait(UserProfile profile) {
        if (profile == null) {
            return true;
        }
        LocalDateTime lastAnalyzedAt = profile.getLastAnalyzedAt();
        if (lastAnalyzedAt == null) {
            return true;
        }
        return Duration.between(lastAnalyzedAt, LocalDateTime.now()).compareTo(PORTRAIT_REFRESH_INTERVAL) > 0;
    }

    private String mapBudgetLevel(Integer budget) {
        if (budget == null) {
            return "未知";
        }
        return switch (budget) {
            case 1 -> "经济型";
            case 2 -> "舒适型";
            case 3 -> "奢华型";
            default -> "未知";
        };
    }

    private String resolveUserLocation(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getRegionId() == null) {
            return "未知地区";
        }
        try {
            var region = regionMapper.selectById(user.getRegionId());
            if (region != null && region.getName() != null && !region.getName().isBlank()) {
                return region.getName();
            }
        } catch (Exception ignored) {
            // ignore
        }
        return "未知地区";
    }

    /** 优先返回 LLM 缓存摘要；缺失时返回模板兜底并异步触发 LLM 生成更自然的版本。 */
    private String buildSummary(Long userId, UserProfile profile, String travelStyle, String budgetLabel,
            String preferredSeason, List<String> preferredTags, List<String> recentPreferences, String location) {
        String cached = userPortraitAnalyzer.getCachedSummary(userId);
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        if (profile == null) {
            return "暂无足够数据生成画像";
        }
        userPortraitAnalyzer.asyncRefreshSummary(userId, PortraitSummaryContext.builder()
                .travelStyle(travelStyle)
                .budgetLabel(budgetLabel)
                .preferredSeason(preferredSeason)
                .preferredTags(preferredTags)
                .recentPreferences(recentPreferences)
                .location(location)
                .build());
        return buildTemplateSummary(travelStyle, budgetLabel, preferredSeason, recentPreferences);
    }

    private String buildTemplateSummary(String travelStyle, String budgetLabel, String preferredSeason,
            List<String> recentPreferences) {
        StringBuilder sb = new StringBuilder("近期更偏好");
        sb.append(travelStyle == null || travelStyle.isBlank() ? "多元体验" : travelStyle);
        if (budgetLabel != null && !"未知".equals(budgetLabel)) {
            sb.append("，预算偏").append(budgetLabel);
        }
        if (preferredSeason != null && !preferredSeason.isBlank()) {
            sb.append("，常在").append(preferredSeason).append("出行");
        }
        if (recentPreferences != null && !recentPreferences.isEmpty()) {
            int limit = Math.min(3, recentPreferences.size());
            sb.append("，关注").append(String.join("、", recentPreferences.subList(0, limit)));
        }
        sb.append("。");
        return sb.toString();
    }

    private List<String> buildRecentPreferences(Long userId) {
        // 1) 浏览：仅取最近30条，按景点聚合次数
        List<UserBrowseHistory> recentBrowseList = userBrowseHistoryMapper.selectList(
                Wrappers.<UserBrowseHistory>lambdaQuery()
                        .eq(UserBrowseHistory::getUserId, userId)
                        .isNotNull(UserBrowseHistory::getScenicSpotId)
                        .orderByDesc(UserBrowseHistory::getCreateTime)
                        .orderByDesc(UserBrowseHistory::getId)
                        .last("LIMIT " + RECENT_BROWSE_LIMIT));

        Map<Long, Long> scenicBrowseCountMap = recentBrowseList.stream()
                .collect(Collectors.groupingBy(UserBrowseHistory::getScenicSpotId, Collectors.counting()));

        // 2) 收藏：按景点聚合次数（同景点多收藏夹也能正确累加）
        List<UserFavorite> favoriteList = userFavoriteMapper.selectList(
                Wrappers.<UserFavorite>lambdaQuery()
                        .eq(UserFavorite::getUserId, userId)
                        .isNotNull(UserFavorite::getScenicSpotId));
        Map<Long, Long> scenicFavoriteCountMap = favoriteList.stream()
                .collect(Collectors.groupingBy(UserFavorite::getScenicSpotId, Collectors.counting()));

        // 3) 评论：按景点聚合次数
        List<UserReview> reviewList = userReviewMapper.selectList(
                Wrappers.<UserReview>lambdaQuery()
                        .eq(UserReview::getUserId, userId)
                        .isNotNull(UserReview::getScenicSpotId));
        Map<Long, Long> scenicReviewCountMap = reviewList.stream()
                .collect(Collectors.groupingBy(UserReview::getScenicSpotId, Collectors.counting()));

        // 4) 先聚合到景点分：scenicScore = browse*1 + favorite*3 + review*5
        Map<Long, Double> scenicScoreMap = new HashMap<>();
        scenicBrowseCountMap
                .forEach((scenicId, count) -> scenicScoreMap.merge(scenicId, count * BROWSE_WEIGHT, Double::sum));
        scenicFavoriteCountMap
                .forEach((scenicId, count) -> scenicScoreMap.merge(scenicId, count * FAVORITE_WEIGHT, Double::sum));
        scenicReviewCountMap
                .forEach((scenicId, count) -> scenicScoreMap.merge(scenicId, count * REVIEW_WEIGHT, Double::sum));

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

    /**
     * 获取我的偏好标签列表
     */
    @Override
    public List<Tag> getMyPreferenceTags() {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        List<UserPreferenceTag> prefTags = userPreferenceTagMapper.selectList(
                Wrappers.<UserPreferenceTag>lambdaQuery().eq(UserPreferenceTag::getUserId, userId));
        if (prefTags.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> tagIds = prefTags.stream().map(UserPreferenceTag::getTagId).collect(Collectors.toList());
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        resolveTagIconUrls(tags);
        return tags;
    }

    /** 与 {@code TagServiceImpl.resolveTagIconUrls} 逻辑一致：将 icon 字段中的 fileResourceId 原地表调为 URL。 */
    private void resolveTagIconUrls(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        List<Long> iconIds = tags.stream()
                .map(Tag::getIcon)
                .map(FileResourceIds::tryParseId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (iconIds.isEmpty()) {
            return;
        }
        Map<Long, String> urlMap = fileService.resolveUrls(iconIds);
        for (Tag tag : tags) {
            Long id = FileResourceIds.tryParseId(tag.getIcon());
            if (id == null) {
                continue;
            }
            String url = urlMap.get(id);
            if (url != null) {
                tag.setIcon(url);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePreferenceTags(UserPreferenceTagsUpdateDTO dto) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        // 1. 删除旧的偏好标签
        userPreferenceTagMapper
                .delete(Wrappers.<UserPreferenceTag>lambdaQuery().eq(UserPreferenceTag::getUserId, userId));
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
