package com.travel.advisor.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReviewCreateDTO {

    @NotNull(message = "景点ID不能为空")
    private Long scenicId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer score;

    private String content;

    private List<Long> imageIds;

    private LocalDate visitDate;

    private String travelType;

    private Integer isAnonymous;
}
