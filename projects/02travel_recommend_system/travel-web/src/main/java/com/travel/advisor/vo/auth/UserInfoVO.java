package com.travel.advisor.vo.auth;

import lombok.Data;

/**
 * 用户信息VO
 */
@Data
public class UserInfoVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;
}
