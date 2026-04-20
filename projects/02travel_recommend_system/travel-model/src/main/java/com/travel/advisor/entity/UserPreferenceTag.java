package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户偏好标签实体
 */
@Data
@TableName("user_preference_tag")
public class UserPreferenceTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long tagId;

    /** 偏好权重（0-1） */
    private BigDecimal weight;

    /** 来源：1 手动选择 2 行为分析 3 LLM 推断 */
    private Integer source;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
