package com.travel.advisor.dto.scenic;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ScenicStatusDTO {

    private List<Long> scenicIds;

    @NotNull(message = "状态不能为空")
    private Integer status;
}