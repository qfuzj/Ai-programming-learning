package com.travel.advisor.vo.region;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 地区树形结构 VO
 */
@Data
public class RegionTreeVO {

    /**
     * 地区 ID
     */
    private Long id;

    /**
     * 父地区 ID，顶级地区的 parentId 为 null 或 0，具体取值根据实际数据而定。
     */
    private Long parentId;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 地区简称
     */
    private String shortName;

    /**
     * 地区层级 1 省 2 市 3 区县
     */
    private Integer level;

    /**
     * 地区编码
     */
    private String code;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 是否热门
     */
    private Integer isHot;

    /**
     * 子地区列表
     */
    private List<RegionTreeVO> children;
}
