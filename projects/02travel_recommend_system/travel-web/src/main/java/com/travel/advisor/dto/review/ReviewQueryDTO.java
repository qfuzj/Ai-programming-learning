package com.travel.advisor.dto.review;

import com.travel.advisor.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewQueryDTO extends PageQuery {

    private String sortBy;
}
