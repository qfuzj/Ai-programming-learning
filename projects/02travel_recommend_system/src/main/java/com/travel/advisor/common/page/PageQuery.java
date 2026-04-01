package com.travel.advisor.common.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQuery {

    @Min(value = 1, message = "pageNum 不能小于 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize 不能小于 1")
    @Max(value = 200, message = "pageSize 不能大于 200")
    private Integer pageSize = 10;

    private String keyword;
    private String sortBy;
    private String sortOrder;
}
