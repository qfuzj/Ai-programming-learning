package com.travel.advisor.security;

import com.travel.advisor.config.JwtProperties;
import com.travel.advisor.utils.JwtUtils;
import com.travel.advisor.utils.RedisUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * TokenService 负责 JWT token 的生成、验证、刷新和失效控制。它与 JwtUtils 紧密配合，利用 Redis 存储 refreshToken 和 accessToken 黑名单信息。
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    public static final String USER_TOKEN_PREFIX = "auth:user:token:";
    public static final String ADMIN_TOKEN_PREFIX = "auth:admin:token:";
    public static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final RedisUtils redisUtils;

    /**
     * 生成一对 accessToken 和 refreshToken，并将 refreshToken 存入 Redis 以供后续校验。
     *
     * @param loginUser 登录用户信息，包含 userId、username、roleType、roleCode、loginType 等字段。
     * @return 包含 accessToken、refreshToken、tokenId 和 expiresIn 的 TokenPair 对象。
     */
    public TokenPair createTokenPair(LoginUser loginUser) {
        // 一次登录会话对应一个 tokenId，用于 refreshToken 校验与后续失效控制。
        String tokenId = UUID.randomUUID().toString();
        loginUser.setTokenId(tokenId);

        String accessToken = jwtUtils.generateAccessToken(loginUser);
        String refreshToken = jwtUtils.generateRefreshToken(loginUser);

        // 仅把 refreshToken 存入 Redis；accessToken 由 JWT 自包含并配合黑名单校验。
        String tokenKey = buildTokenKey(loginUser.getRoleType(), loginUser.getUserId(), tokenId);
        redisUtils.set(tokenKey, refreshToken, Duration.ofSeconds(jwtProperties.getRefreshTokenExpireSeconds()));

        return new TokenPair(accessToken, refreshToken, tokenId, jwtProperties.getAccessTokenExpireSeconds());
    }

    public boolean verifyRefreshToken(LoginUser loginUser, String tokenId, String refreshToken) {
        // 只有“前端提交的 refreshToken 与 Redis中保存值完全一致”才允许刷新。
        String tokenKey = buildTokenKey(loginUser.getRoleType(), loginUser.getUserId(), tokenId);
        String stored = redisUtils.get(tokenKey);
        return refreshToken.equals(stored);
    }

    /**
     * 添加 tokenId 到黑名单，并删除 Redis 中对应的 refreshToken 记录。黑名单只存储 accessToken 的 tokenId，过期后由 Redis 自动淘汰。
     * @param loginUser
     * @param tokenId
     * @param remainingSeconds
     */
    public void invalidateToken(LoginUser loginUser, String tokenId, long remainingSeconds) {
        String tokenKey = buildTokenKey(loginUser.getRoleType(), loginUser.getUserId(), tokenId);
        redisUtils.delete(tokenKey);
        if (remainingSeconds > 0) {
            // 黑名单只存 accessToken 的剩余时长，过期后由 Redis 自动淘汰。
            redisUtils.set(BLACKLIST_PREFIX + tokenId, 1, Duration.ofSeconds(remainingSeconds));
        }
    }

    public boolean isBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(redisUtils.hasKey(BLACKLIST_PREFIX + tokenId));
    }

    public LoginUser parseLoginUser(String token) {
        Claims claims = jwtUtils.parseToken(token);
        return LoginUser.builder()
                .userId(claims.get("userId", Long.class))
                .username(claims.get("username", String.class))
                .roleType(claims.get("roleType", String.class))
                .roleCode(claims.get("roleCode", String.class))
                .loginType(claims.get("loginType", String.class))
                .tokenId(claims.getId())
                .build();
    }

    public Claims parseClaims(String token) {
        return jwtUtils.parseToken(token);
    }

    private String buildTokenKey(String roleType, Long userId, String tokenId) {
        String prefix = "ADMIN".equalsIgnoreCase(roleType) ? ADMIN_TOKEN_PREFIX : USER_TOKEN_PREFIX;
        return prefix + userId + ":" + tokenId;
    }

    public record TokenPair(String accessToken, String refreshToken, String tokenId, Long expiresIn) {
    }
}
