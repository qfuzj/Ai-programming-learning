package com.travel.advisor.service;

import java.util.List;

import com.travel.advisor.dto.user.UserPreferenceTagsUpdateDTO;
import com.travel.advisor.dto.user.UserProfileUpdateDTO;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.vo.user.UserProfilePortraitVO;
import com.travel.advisor.vo.user.UserProfileVO;

public interface UserProfileService {
    UserProfileVO getMyProfile();
    void updateMyProfile(UserProfileUpdateDTO dto);
    UserProfilePortraitVO getMyPortrait();
    void updatePreferenceTags(UserPreferenceTagsUpdateDTO dto);
    List<Tag> getMyPreferenceTags();
}
