package com.travel.advisor.vo.auth;

import lombok.Data;

/**
 * 登录成功后返回给前端的 VO 对象
 */
@Data
public class LoginVO {

    /**
     * 访问令牌，短时有效，前端每次请求带 Bearer 使用。
     */
    private String accessToken;
    /**
     * 刷新令牌，用来换新 accessToken。
     */
    private String refreshToken;
    /**
     * 通常是 Bearer，告诉前端 Authorization 头怎么拼。
     */
    private String tokenType;
    /**
     * accessToken 过期秒数，前端可据此安排刷新策略。
     */
    private Long expiresIn;
    /**
     * 用户端登录返回用户信息。普通用户登录时会填这个。
     */
    private UserInfoVO userInfo;
    /**
     * 管理员登录时返回管理员信息。管理员登录时会填这个。
     */
    private AdminInfoVO adminInfo;
    /**
     * 用户角色类型，普通用户登录时为 USER，管理员登录时为 ADMIN。前端可据此区分登录类型。
     */
    private String roleCode;
}
