package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("travel_plan_item")
public class TravelPlanItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long travelPlanId;

    private Long scenicSpotId;

    @TableField("day_number")
    private Integer dayNo;

    private Integer sortOrder;

    /**
     * 类型：1 景点 2 餐饮 3 住宿 4 交通 5 自定义
     */
    private Integer itemType;

    private String title;

    private String description;

    private LocalTime startTime;

    private LocalTime endTime;

    private String location;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal estimatedCost;

    private String notes;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
