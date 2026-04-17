package com.travel.advisor.dto.config;

import com.travel.advisor.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemConfigQueryDTO extends PageQuery {

    /**
     * 配置分组 （basic / llm / recommend / minio 等）默认为default
     */
    private String configGroup;
}
