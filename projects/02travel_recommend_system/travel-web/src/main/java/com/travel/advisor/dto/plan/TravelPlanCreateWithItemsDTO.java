package com.travel.advisor.dto.plan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 一体化创建行程 DTO
 */
@Data
public class TravelPlanCreateWithItemsDTO {

    @NotNull(message = "行程信息不能为空")
    @Valid
    private TravelPlanCreateDTO plan;

    @Valid
    private List<TravelPlanItemCreateDTO> items = new ArrayList<>();
}
