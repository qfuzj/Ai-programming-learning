package com.campus.resourcesharing.common.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQuery {

    @Min(value = 1, message = "pageNum 最小为 1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize 最小为 1")
    @Max(value = 100, message = "pageSize 最大为 100")
    private Long pageSize = 10L;

    public <T> Page<T> toPage() {
        return new Page<>(pageNum, pageSize);
    }
}
