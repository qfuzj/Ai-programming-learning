package com.travel.advisor.vo.auth;

import lombok.Data;

/**
 * 管理员信息VO对象
 */
@Data
public class AdminInfoVO {

    /**
     * 管理员ID
     */
    private Long adminId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 头像URL
     */
    private String avatar;
}
