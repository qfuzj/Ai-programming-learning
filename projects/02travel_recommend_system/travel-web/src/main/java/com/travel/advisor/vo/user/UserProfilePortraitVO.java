package com.travel.advisor.vo.user;

import lombok.Data;
import java.util.List;

/**
 * 用户画像 VO 对象，包含用户的旅行风格、预算水平、偏好标签、近期偏好等信息。
 */
@Data
public class UserProfilePortraitVO {
    private String travelStyle;
    private String budgetLevel;
    private List<String> preferredTags;
    private List<String> recentPreferences;
    private String summary;
    private String location;
}
