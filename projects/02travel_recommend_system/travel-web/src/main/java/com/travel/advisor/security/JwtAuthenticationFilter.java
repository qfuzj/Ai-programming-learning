package com.travel.advisor.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // 无 token 的请求直接放行，是否允许访问由 SecurityConfig 的路由规则决定。
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = tokenService.parseClaims(token);
            String tokenId = claims.getId();
            // 命中黑名单表示已登出或已失效，不再为其建立认证上下文。
            if (tokenService.isBlacklisted(tokenId)) {
                filterChain.doFilter(request, response);
                return;
            }

            LoginUser loginUser = tokenService.parseLoginUser(token);
            // 将登录态写入 SecurityContext，后续权限判断基于 ROLE_USER/ROLE_ADMIN。
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginUser,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + loginUser.getRoleType()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException ex) {
            log.debug("JWT parse failed: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs");
    }
}
