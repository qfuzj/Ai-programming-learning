package com.travel.advisor.dto.region;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 地区创建 DTO
 */
@Data
public class RegionCreateDTO {

    @NotNull(message = "父级地区ID不能为空")
    private Long parentId;

    @NotBlank(message = "地区名称不能为空")
    private String name;

    private String shortName;

    @NotNull(message = "层级不能为空")
    private Integer level;

    private String code;

    private String pinyin;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer sortOrder;

    private Integer isHot;
}
