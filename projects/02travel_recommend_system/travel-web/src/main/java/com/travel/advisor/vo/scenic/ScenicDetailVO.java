package com.travel.advisor.vo.scenic;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 景点详细信息 VO
 */
@Data
public class ScenicDetailVO {

    /**
     * 景点 ID
     */
    private Long scenicId;

    /**
     * 景点名称
     */
    private String name;

    /**
     * 所属地区 ID
     */
    private Long regionId;

    /**
     * 所属地区名称
     */
    private String regionName;

    /**
     * 景点地址
     */
    private String address;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 封面图片 URL
     */
    private String coverImage;

    /**
     * 景点简介描述
     */
    private String description;

    /**
     * 详细内容
     */
    private String detailContent;

    /**
     * 开放时间
     */
    private String openTime;

    /**
     * 票价信息
     */
    private String ticketInfo;

    /**
     * 票价
     */
    private BigDecimal ticketPrice;

    /**
     * 景点级别
     */
    private String level;

    /**
     * 景点分类
     */
    private String category;

    /**
     * 综合评分（与列表中的 score 保持一致）
     */
    private Double score;

    /**
     * 评分得分
     */
    private Double ratingScore;

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
     * 评论数（当前与 ratingCount 对齐）
     */
    private Integer reviewCount;

    /**
     * 热度分（当前使用收藏+浏览的简单聚合）
     */
    private BigDecimal hotScore;

    /**
     * 最佳旅游季节
     */
    private String bestSeason;

    /**
     * 建议游玩小时数
     */
    private Integer suggestedHours;

    /**
     * 游玩小贴士
     */
    private String tips;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否推荐：0-不推荐，1-推荐
     */
    private Integer isRecommended;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 标签 ID 列表
     */
    private List<Long> tagIds;

    /**
     * 图片列表
     */
    private List<ScenicImageVO> images;

    /**
     * 是否被当前用户收藏
     */
    private Boolean isFavorite;
}
