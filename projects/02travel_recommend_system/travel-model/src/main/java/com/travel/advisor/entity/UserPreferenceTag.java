package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_preference_tag")
public class UserPreferenceTag {
    private Long userId;
    private Long tagId;
}
