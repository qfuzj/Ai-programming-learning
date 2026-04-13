package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.travel.advisor.dto.user.UserPreferenceTagsUpdateDTO;
import com.travel.advisor.dto.user.UserProfileUpdateDTO;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.entity.User;
import com.travel.advisor.entity.UserPreferenceTag;
import com.travel.advisor.entity.UserProfile;
import com.travel.advisor.mapper.TagMapper;
import com.travel.advisor.mapper.UserMapper;
import com.travel.advisor.mapper.UserPreferenceTagMapper;
import com.travel.advisor.mapper.UserProfileMapper;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserPreferenceTagMapper userPreferenceTagMapper;
    private final TagMapper tagMapper;

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
        
        vo.setRecentPreferences(new ArrayList<>());
        vo.setLocation("未知地区");
        return vo;
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
