package com.travel.advisor.common.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    @Builder.Default
    private List<T> records = Collections.emptyList();

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Long totalPage;
}
