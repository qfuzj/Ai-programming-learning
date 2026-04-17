package com.travel.advisor.dto.scenic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 景点创建 DTO
 */
@Data
public class ScenicCreateDTO {

    @NotBlank(message = "景点名称不能为空")
    private String name;

    @NotNull(message = "地区ID不能为空")
    private Long regionId;

    @NotBlank(message = "地址不能为空")
    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String coverImage;

    private String description;

    private String detailContent;

    private String openTime;

    private String ticketInfo;

    private BigDecimal ticketPrice;

    private String level;

    private String category;

    private String bestSeason;

    private Integer suggestedHours;

    private String tips;

    private Integer status;

    private Integer sortOrder;

    private Integer isRecommended;

    private List<Long> tagIds;

    private List<Long> imageIds;
}
