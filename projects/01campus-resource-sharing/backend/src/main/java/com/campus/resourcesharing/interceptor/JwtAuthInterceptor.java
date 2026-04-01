package com.campus.resourcesharing.interceptor;

import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行预检请求，避免浏览器跨域 OPTIONS 校验被拦截
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或token失效");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException(401, "未登录或token失效");
        }
        return true;
    }
}
