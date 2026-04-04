package com.travel.advisor.security;

import com.travel.advisor.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 权限服务，提供基于当前登录用户的权限判断方法
 */
@Service("permissionService")
public class PermissionService {

    public boolean isAdmin() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser != null && "ADMIN".equalsIgnoreCase(loginUser.getRoleType());
    }

    public boolean isUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser != null && "USER".equalsIgnoreCase(loginUser.getRoleType());
    }

    public boolean hasRole(String roleCode) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser != null && StringUtils.hasText(roleCode)
            && roleCode.equalsIgnoreCase(loginUser.getRoleCode());
    }
}
