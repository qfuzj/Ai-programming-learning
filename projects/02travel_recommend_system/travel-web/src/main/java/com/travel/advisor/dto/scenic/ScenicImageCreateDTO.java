package com.travel.advisor.dto.scenic;

import lombok.Data;

/**
 * 景点图片创建 DTO
 */
@Data
public class ScenicImageCreateDTO {

    private Long fileResourceId;

    private String imageUrl;

    private Integer imageType;

    private String title;

    private Integer sortOrder;

    private Integer isCover;
}