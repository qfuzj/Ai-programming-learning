package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("scenic_spot")
public class ScenicSpot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 景点名称
     */
    private String name;

    /**
     * 所属地区ID
     */
    private Long regionId;

    /**
     * 景点地址
     */
    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 景点简介
     */
    private String description;

    /**
     * 景点详情内容，可能包含HTML格式
     */
    private String detailContent;

    /**
     * 开放时间说明，如"每天9:00-17:00"
     */
    private String openTime;

    /**
     * 门票信息说明，如"成人票100元，儿童票50元"
     */
    private String ticketInfo;

    /**
     * 平均门票价格，单位为元
     */
    private BigDecimal ticketPrice;

    /**
     * 景区等级：5A/4A/3A 等
     */
    private String level;

    /**
     * 景点类型，如"自然风光"、"历史文化"、"主题公园"等
     */
    private String category;

    /**
     * 综合评分（1-5）
     */
    private Double score;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 最佳旅游季节，如"春季"、"夏季"、"秋季"、"冬季"
     */
    private String bestSeason;

    /**
     * 建议游玩时长，单位为小时
     */
    private Integer suggestedHours;

    /**
     * 旅游小贴士，如"建议早上前往，避开人流高峰"
     */
    private String tips;

    /**
     * 景点状态：0-不可见/下架，1-正常 2 审核中
     */
    private Integer status;

    /**
     * 排序字段，数值越小越靠前
     */
    private Integer sortOrder;

    /**
     * 是否推荐：0-否，1-是
     */
    private Integer isRecommended;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}