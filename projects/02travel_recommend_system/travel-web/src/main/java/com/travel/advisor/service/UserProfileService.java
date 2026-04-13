package com.travel.advisor.service;

import com.travel.advisor.dto.user.UserPreferenceTagsUpdateDTO;
import com.travel.advisor.dto.user.UserProfileUpdateDTO;
import com.travel.advisor.vo.user.UserProfilePortraitVO;
import com.travel.advisor.vo.user.UserProfileVO;

public interface UserProfileService {
    UserProfileVO getMyProfile();
    void updateMyProfile(UserProfileUpdateDTO dto);
    UserProfilePortraitVO getMyPortrait();
    void updatePreferenceTags(UserPreferenceTagsUpdateDTO dto);
    java.util.List<com.travel.advisor.entity.Tag> getMyPreferenceTags();
}
