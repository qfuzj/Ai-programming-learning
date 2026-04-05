package com.travel.advisor.vo.favorite;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteVO {

    private Long scenicId;

    private String scenicName;

    private String coverImage;

    private BigDecimal score;

    private LocalDateTime favoriteTime;
}
