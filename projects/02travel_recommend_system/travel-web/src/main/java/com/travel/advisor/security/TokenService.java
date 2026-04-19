package com.travel.advisor.security;

import com.travel.advisor.config.JwtProperties;
import com.travel.advisor.utils.JwtUtils;
import com.travel.advisor.utils.RedisUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
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
    /**
     * 已使用的 refreshToken 标记前缀。用于 refresh token rotation 场景下检测重放：
     * 旧 refreshToken 一旦被消费就在此处打一个和原 refresh 同生命周期的标记，
     * 下次若再看到同一 tokenId 提交，即视为"旧 token 被重复使用"——属于可疑重放。
     */
    public static final String REFRESH_USED_PREFIX = "auth:refresh:used:";

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

        // 仅把 refreshToken 存入 Redis；accessToken 由 JWT 自包含并配合黑名单校验。 key: auth:{roleType}:token:{userId}:{tokenId} -> refreshToken
        String tokenKey = buildTokenKey(loginUser.getRoleType(), loginUser.getUserId(), tokenId);
        redisUtils.set(tokenKey, refreshToken, Duration.ofSeconds(jwtProperties.getRefreshTokenExpireSeconds()));

        return new TokenPair(accessToken, refreshToken, tokenId, jwtProperties.getAccessTokenExpireSeconds());
    }

    /**
     * 验证 refreshToken 的合法性。要求 JWT 解析成功且 Redis 中存在对应 tokenId 的 refreshToken 记录，并且两者完全匹配。
     */
    public boolean verifyRefreshToken(LoginUser loginUser, String tokenId, String refreshToken) {
        // 只有“前端提交的 refreshToken 与 Redis中保存值完全一致”才允许刷新。
        String tokenKey = buildTokenKey(loginUser.getRoleType(), loginUser.getUserId(), tokenId);
        String stored = redisUtils.get(tokenKey);
        return refreshToken.equals(stored);
    }

    /**
     * 添加 tokenId 到黑名单，并删除 Redis 中对应的 refreshToken 记录。黑名单只存储 accessToken 的 tokenId，过期后由 Redis 自动淘汰。
     * @param loginUser 登录用户信息，包含 userId、username、roleType、roleCode、loginType 等字段。
     * @param tokenId 需要失效的 tokenId，通常来自 JWT 的 claims.getId()，确保与生成 token 时一致。
     * @param remainingSeconds accessToken 的剩余有效时间，单位为秒，用于设置黑名单的过期时间，确保 accessToken 过期后黑名单记录自动清理。
     */
    public void invalidateToken(LoginUser loginUser, String tokenId, long remainingSeconds) {
        String tokenKey = buildTokenKey(loginUser.getRoleType(), loginUser.getUserId(), tokenId);
        redisUtils.delete(tokenKey);
        if (remainingSeconds > 0) {
            // 黑名单只存 accessToken 的剩余时长，过期后由 Redis 自动淘汰。
            redisUtils.set(BLACKLIST_PREFIX + tokenId, 1, Duration.ofSeconds(remainingSeconds));
        }
    }

    public void invalidateUserSessions(Long userId) {
        invalidateSessions(USER_TOKEN_PREFIX + userId + ":");
    }

    public void invalidateAdminSessions(Long adminId) {
        invalidateSessions(ADMIN_TOKEN_PREFIX + adminId + ":");
    }

    public boolean isBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(redisUtils.hasKey(BLACKLIST_PREFIX + tokenId));
    }

    /**
     * 把一枚 refreshToken 标记为"已使用"。TTL 应与原 refreshToken 剩余有效期一致，
     * 这样即使攻击者事后拿到旧 refreshToken，也能在其自然过期前被检测出重放。
     *
     * @param tokenId    refreshToken 的 jti
     * @param ttlSeconds 剩余有效期秒数；&lt;=0 时不写入（token 已过期无需再标记）
     */
    public void markRefreshTokenUsed(String tokenId, long ttlSeconds) {
        if (tokenId == null || tokenId.isEmpty() || ttlSeconds <= 0) {
            return;
        }
        redisUtils.set(REFRESH_USED_PREFIX + tokenId, 1, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 判断某 refreshToken 是否已被消费过——用于 refresh rotation 场景的重放检测。
     */
    public boolean isRefreshTokenUsed(String tokenId) {
        if (tokenId == null || tokenId.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisUtils.hasKey(REFRESH_USED_PREFIX + tokenId));
    }

    /**
     * 从 JWT token 中解析出 LoginUser 对象。该方法会验证 token 的有效性（如签名、过期等），并提取其中的用户信息字段。
     */
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

    private void invalidateSessions(String tokenKeyPrefix) {
        Set<String> tokenKeys = redisUtils.scanKeys(tokenKeyPrefix + "*");
        if (tokenKeys == null || tokenKeys.isEmpty()) {
            return;
        }

        for (String tokenKey : tokenKeys) {
            redisUtils.delete(tokenKey);
            // tokenKey 格式：auth:{roleType}:token:{userId}:{tokenId}，提取 tokenId 以加入黑名单。
            String tokenId = tokenKey.substring(tokenKey.lastIndexOf(':') + 1);
            // 失效当前 tokenId 对应的 accessToken，加入黑名单，TTL = accessToken 的过期时间，确保其自然过期后自动清理。
            redisUtils.set(BLACKLIST_PREFIX + tokenId, 1, Duration.ofSeconds(jwtProperties.getAccessTokenExpireSeconds()));
        }
    }

    public record TokenPair(String accessToken, String refreshToken, String tokenId, Long expiresIn) {
    }
}
