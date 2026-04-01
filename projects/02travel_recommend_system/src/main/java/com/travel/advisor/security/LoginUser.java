package com.travel.advisor.security;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录用户信息
 */
@Data
@Builder
public class LoginUser implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 角色类型，ADMIN 或 USER，供权限系统识别使用。
     */
    private String roleType;
    /**
     * 角色编码，管理员为具体角色名称（如 SUPER_ADMIN、CONTENT_MANAGER 等），用户为 VIP、REGULAR 等等级标识，供业务系统使用。
     */
    private String roleCode;
    /**
     * 登录方式标记，供业务系统区分不同登录场景（如 username、phone、oauth 等），并可据此调整后续逻辑（如权限校验、日志记录等）。
     */
    private String loginType;
    /**
     * tokenId 用于关联 accessToken 与 refreshToken，便于后续刷新与失效控制。正常流程下由 TokenService 注入，兜底逻辑用于避免空值。
     */
    private String tokenId;
}
