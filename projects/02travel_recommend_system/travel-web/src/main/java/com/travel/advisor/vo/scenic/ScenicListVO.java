package com.travel.advisor.vo.scenic;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 景点列表项 VO
 */
@Data
public class ScenicListVO {

    /**
     * 景点 ID
     */
    private Long scenicId;

    /**
     * 景点名称
     */
    private String name;

    /**
     * 景点封面图 URL
     */
    private String coverImage;

    /**
     * 景点所在城市
     */
    private String regionName;

    /**
     * 景点评分
     */
    private BigDecimal score;

    /**
     * 景点级别
     */
    private String level;

    /**
     * 景点标签列表
     */
    private List<String> tagList;

    /**
     * 是否是用户的收藏
     */
    private Boolean isFavorite;
}