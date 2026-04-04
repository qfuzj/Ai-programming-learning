package com.travel.advisor.dto.scenic;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ScenicUpdateDTO {

    private String name;

    private Long regionId;

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

    private BigDecimal ratingScore;

    private String bestSeason;

    private Integer suggestedHours;

    private String tips;

    private Integer status;

    private Integer sortOrder;

    private Integer isRecommended;

    private List<Long> tagIds;

    private List<Long> imageIds;
}