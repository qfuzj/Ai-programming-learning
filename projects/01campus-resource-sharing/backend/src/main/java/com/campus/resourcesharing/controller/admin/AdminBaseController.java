package com.campus.resourcesharing.controller.admin;

import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.utils.JwtUtil;
import io.jsonwebtoken.Claims;

public abstract class AdminBaseController {

    protected final JwtUtil jwtUtil;

    protected AdminBaseController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    protected void assertAdmin(String authorization) {
        String token = extractToken(authorization);
        Claims claims = jwtUtil.parseToken(token);
        String role = claims.get("role", String.class);
        if (role == null || !"admin".equalsIgnoreCase(role.trim())) {
            throw new BusinessException(403, "无管理员权限");
        }
    }

    protected String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new BusinessException(401, "未登录或token失效");
        }
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }
}
