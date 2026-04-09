package com.travel.advisor.dto.log;

import com.travel.advisor.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogQueryDTO extends PageQuery {

    /**
     * 操作模块 (scenic / user / review / config等)
     */
    private String module;

    /**
     * 操作类型 （create / update / delete / audit / export 等）
     */
    private String action;

    /**
     * 操作状态 (0-失败, 1-成功)
     */
    private Integer status;

    /**
     * 操作员用户名
     */
    private String adminUsername;
}
