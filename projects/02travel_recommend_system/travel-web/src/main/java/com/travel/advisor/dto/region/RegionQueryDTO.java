package com.travel.advisor.dto.region;

import lombok.Data;

/**
 * 地区查询 DTO
 */
@Data
public class RegionQueryDTO {
    private String name;
    private Integer level;
    private String code;
    private Integer isHot;
    private Long parentId;
}
