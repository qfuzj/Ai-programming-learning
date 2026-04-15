package com.travel.advisor.vo.auth;

import lombok.Data;

/**
 * 注册成功后返回给前端的 VO 对象
 */
@Data
public class RegisterVO {

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String username;
}
