package com.travel.advisor.vo.scenic;

import lombok.Data;

@Data
public class ScenicImageVO {

    private Long id;

    private Long fileResourceId;

    private String imageUrl;

    private Integer imageType;

    private String title;

    private Integer sortOrder;

    private Integer isCover;
}