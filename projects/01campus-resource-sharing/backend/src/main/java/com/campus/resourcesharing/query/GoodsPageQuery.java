package com.campus.resourcesharing.query;

import com.campus.resourcesharing.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsPageQuery extends PageQuery {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String conditionLevel;
    private String sortType;
}
