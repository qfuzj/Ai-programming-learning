package com.travel.advisor.dto.user;

import com.travel.advisor.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户管理查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQuery {

    /**
     * 账号状态：0-禁用 1-正常
     */
    private Integer status;
}
