package com.travel.advisor.utils;

import com.travel.advisor.config.JwtProperties;
import com.travel.advisor.security.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具类，负责生成和解析 JWT Token。AccessToken 和 RefreshToken 共用同一套签名算法和密钥，但通过 tokenType 区分。
 */
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // 统一使用配置中的密钥初始化签名器，access/refresh 共用同一签名算法。
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(LoginUser loginUser) {
        return buildToken(loginUser, jwtProperties.getAccessTokenExpireSeconds(), "access");
    }

    public String generateRefreshToken(LoginUser loginUser) {
        return buildToken(loginUser, jwtProperties.getRefreshTokenExpireSeconds(), "refresh");
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 构建 JWT Token 的通用方法，根据传入的过期时间和 tokenType 生成对应的 AccessToken 或 RefreshToken。
     *
     * @param loginUser     登录用户信息，包含 userId、username、roleType、roleCode、loginType 等字段。
     * @param expireSeconds token 过期时间，单位为秒，由调用方根据 access/refresh 需求传入不同值。
     * @param tokenType     token 类型标识，"access" 或 "refresh"，会被写入 JWT 的 claims 中，供后续解析时区分使用场景。
     * @return 生成的 JWT Token 字符串，包含用户信息和 tokenType 以供后续验证和权限判断。
     */
    private String buildToken(LoginUser loginUser, Long expireSeconds, String tokenType) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(expireSeconds);
        // 正常流程下 tokenId 由 TokenService 注入，兜底逻辑用于避免空值。
        String tokenId = loginUser.getTokenId() == null ? UUID.randomUUID().toString() : loginUser.getTokenId();

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(loginUser.getUserId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .id(tokenId)
                // tokenType 用于区分 access 与 refresh，避免拿错 token 调用刷新接口。
                .claims(Map.of(
                        "userId", loginUser.getUserId(),
                        "username", loginUser.getUsername(),
                        "roleType", loginUser.getRoleType(),
                        "roleCode", loginUser.getRoleCode(),
                        "loginType", loginUser.getLoginType(),
                        "tokenType", tokenType
                ))
                .signWith(secretKey)
                .compact();
    }
}
