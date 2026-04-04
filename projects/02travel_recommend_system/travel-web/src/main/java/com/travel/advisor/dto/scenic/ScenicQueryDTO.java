package com.travel.advisor.dto.scenic;

import com.travel.advisor.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScenicQueryDTO extends PageQuery {

    private Long regionId;

    private String category;

    private String level;

    private BigDecimal minScore;

    private Integer status;
}