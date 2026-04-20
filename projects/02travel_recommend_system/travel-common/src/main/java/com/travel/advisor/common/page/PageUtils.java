package com.travel.advisor.common.page;

import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.exception.BusinessException;

/**
 * 分页工具方法
 */
public final class PageUtils {

    public static final int DEFAULT_MAX_PAGE_SIZE = 100;

    private PageUtils() {
    }

    /**
     * 校验 pageSize 不超过上限
     */
    public static void validatePageSize(Integer pageSize) {
        validatePageSize(pageSize, DEFAULT_MAX_PAGE_SIZE);
    }

    /**
     * 校验 pageSize 不超过指定上限
     */
    public static void validatePageSize(Integer pageSize, int maxSize) {
        if (pageSize != null && pageSize > maxSize) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "pageSize 不能大于 " + maxSize);
        }
    }
}
