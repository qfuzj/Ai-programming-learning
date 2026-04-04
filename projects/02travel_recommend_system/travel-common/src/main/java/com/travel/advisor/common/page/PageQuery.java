package com.travel.advisor.common.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQuery {

    /**
     * 页码，默认值为 1，必须大于等于 1
     */
    @Min(value = 1, message = "pageNum 不能小于 1")
    private Integer pageNum = 1;

    /**
     * 每页记录数，默认值为 10，必须在 1 到 200 之间
     */
    @Min(value = 1, message = "pageSize 不能小于 1")
    @Max(value = 200, message = "pageSize 不能大于 200")
    private Integer pageSize = 10;

    /**
     * 搜索关键词，支持模糊匹配
     */
    private String keyword;
    /**
     * 排序字段，例如 "createTime" 或 "name"，默认值为 "createTime"
     */
    private String sortBy;
    /**
     * 排序方式，ASC 或 DESC，默认值为 DESC
     */
    private String sortOrder;

    public void setPageNum(Integer pageNum) {
        if (pageNum != null) {
            this.pageNum = pageNum;
        }
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize != null) {
            this.pageSize = pageSize;
        }
    }
}
