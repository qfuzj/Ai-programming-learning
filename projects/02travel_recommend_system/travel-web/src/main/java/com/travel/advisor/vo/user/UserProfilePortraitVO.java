package com.travel.advisor.vo.user;

import lombok.Data;
import java.util.List;

@Data
public class UserProfilePortraitVO {
    private String travelStyle;
    private String budgetLevel;
    private List<String> preferredTags;
    private List<String> recentPreferences;
    private String summary;
    private String location;
}
