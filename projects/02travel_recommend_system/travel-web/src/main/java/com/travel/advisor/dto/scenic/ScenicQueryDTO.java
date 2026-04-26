package com.travel.advisor.dto.scenic;

import com.travel.advisor.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 景点查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScenicQueryDTO extends PageQuery {

    private Long regionId;

    private String category;

    private String level;

    private Long tagId;

    private List<Long> tagIds;

    private String tagScope;

    private String tagCategory;

    private Integer status;
}
