package com.campus.resourcesharing.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total;
    private Long pageNum;
    private Long pageSize;
    private List<T> records;

    public static <T> PageResult<T> from(IPage<T> page) {
        return new PageResult<>(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );
    }

    public static <T> PageResult<T> empty(Long pageNum, Long pageSize) {
        return new PageResult<>(0L, pageNum, pageSize, Collections.emptyList());
    }
}
