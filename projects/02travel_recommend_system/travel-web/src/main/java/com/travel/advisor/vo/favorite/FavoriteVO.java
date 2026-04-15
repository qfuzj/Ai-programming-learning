package com.travel.advisor.vo.favorite;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteVO {

    private Long scenicId;

    private String scenicName;

    private String coverImage;

    private Double score;

    private LocalDateTime favoriteTime;
}
