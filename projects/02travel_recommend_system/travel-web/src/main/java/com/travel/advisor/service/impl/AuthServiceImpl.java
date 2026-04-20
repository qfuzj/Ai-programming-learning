package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.auth.RefreshTokenDTO;
import com.travel.advisor.dto.auth.ResetPasswordDTO;
import com.travel.advisor.dto.auth.UserLoginDTO;
import com.travel.advisor.dto.auth.UserRegisterDTO;
import com.travel.advisor.entity.User;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.UserMapper;
import com.travel.advisor.security.LoginUser;
import com.travel.advisor.security.TokenService;
import com.travel.advisor.service.AuthService;
import com.travel.advisor.service.CaptchaService;
import com.travel.advisor.utils.RedisUtils;
import com.travel.advisor.vo.auth.LoginVO;
import com.travel.advisor.vo.auth.RegisterVO;
import com.travel.advisor.vo.auth.UserInfoVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;
    private final TokenService tokenService;
    private final RedisUtils redisUtils;

    private static final String REGISTER_LIMIT_PREFIX = "auth:limit:register:";
    private static final String RESET_LIMIT_PREFIX = "auth:limit:reset:";
    private static final int REGISTER_MAX_PER_HOUR = 5;
    private static final int RESET_MAX_PER_HOUR = 5;

    @Override
    public RegisterVO register(UserRegisterDTO dto) {
        checkRateLimit(REGISTER_LIMIT_PREFIX + dto.getUsername(), REGISTER_MAX_PER_HOUR);
        captchaService.validateCaptcha(dto.getCaptchaId(), dto.getCaptchaCode());
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码输入不一致");
        }
        checkUserExists(dto.getUsername(), dto.getPhone());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getUsername());
        user.setStatus(1);
        userMapper.insert(user);

        RegisterVO vo = new RegisterVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        return vo;
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        // 登录前先校验验证码，并且验证码在校验成功后会立即失效（一次性使用）。
        captchaService.validateCaptcha(dto.getCaptchaId(), dto.getCaptchaCode());

        // 按 loginType(用户名/手机号)查询账号，再进行密码比对。
        User user = findByLoginType(dto);
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前用户已被禁用");
        }

        // 登录成功后记录最后登录时间。
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成一对 accessToken/refreshToken。
        TokenService.TokenPair tokenPair = tokenService.createTokenPair(LoginUser.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .roleType("USER")
            .roleCode("user")
            .loginType(dto.getLoginType())
            .build());

        return buildLoginVO(user, tokenPair);
    }

    @Override
    public LoginVO refreshToken(RefreshTokenDTO dto) {
        Claims claims;
        try {
            claims = tokenService.parseClaims(dto.getRefreshToken());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "refreshToken无效");
        }
        String tokenType = claims.get("tokenType", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "token类型错误");
        }

        LoginUser loginUser = tokenService.parseLoginUser(dto.getRefreshToken());
        String oldTokenId = claims.getId();
        // 计算 refreshToken 的剩余有效期，单位秒。
        long refreshRemaining = Math.max(0,
            claims.getExpiration().toInstant().getEpochSecond() - Instant.now().getEpochSecond());

        // 反重放：若该 refreshToken 已被消费过，认为存在泄漏/窃取风险，直接撤销该用户全部会话。
        if (tokenService.isRefreshTokenUsed(oldTokenId)) {
            // 撤销用户全部会话：删除该用户所有 accessToken/refreshToken 在 Redis 中的记录，使其无法再使用任何旧 token 进行刷新或访问。
            tokenService.invalidateUserSessions(loginUser.getUserId());
            throw new BusinessException(ResultCode.UNAUTHORIZED, "refreshToken已被使用，请重新登录");
        }

        // refreshToken 必须同时满足：JWT合法 + Redis中仍存在同一 tokenId 记录。
        if (!tokenService.verifyRefreshToken(loginUser, oldTokenId, dto.getRefreshToken())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "refreshToken已失效");
        }

        User user = userMapper.selectById(loginUser.getUserId());
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在或已禁用");
        }

        TokenService.TokenPair tokenPair = tokenService.createTokenPair(LoginUser.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .roleType("USER")
            .roleCode("user")
            .loginType(loginUser.getLoginType())
            .build());
        // 标记旧 refreshToken 已使用，TTL = 其剩余有效期；下次再看到同 tokenId 提交会触发反重放分支。
        tokenService.markRefreshTokenUsed(oldTokenId, refreshRemaining);
        // 删除旧 refreshToken 在 Redis 中的存储，避免常规重复刷新。
        tokenService.invalidateToken(loginUser, oldTokenId, 0);
        return buildLoginVO(user, tokenPair);
    }

    @Override
    public void logout(String authorization) {
        String token = extractToken(authorization);
        Claims claims = tokenService.parseClaims(token);
        LoginUser loginUser = tokenService.parseLoginUser(token);
        // accessToken 退出登录后进入黑名单，TTL=accessToken 剩余有效期。
        long remaining = Math.max(0, claims.getExpiration().toInstant().getEpochSecond() - Instant.now().getEpochSecond());
        tokenService.invalidateToken(loginUser, claims.getId(), remaining);
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        checkRateLimit(RESET_LIMIT_PREFIX + dto.getPhone(), RESET_MAX_PER_HOUR);
        captchaService.validateCaptcha(dto.getCaptchaId(), dto.getCaptchaCode());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getPhone, dto.getPhone())
            .last("limit 1"));
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    private User findByLoginType(UserLoginDTO dto) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        // phone 模式用手机号查询，其他模式默认按用户名查询。
        if ("phone".equalsIgnoreCase(dto.getLoginType())) {
            if (!StringUtils.hasText(dto.getPhone())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "手机号不能为空");
            }
            queryWrapper.eq(User::getPhone, dto.getPhone());
        } else {
            if (!StringUtils.hasText(dto.getUsername())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "用户名不能为空");
            }
            queryWrapper.eq(User::getUsername, dto.getUsername());
        }
        queryWrapper.last("limit 1");
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 在注册前检查用户名或手机号是否已存在，并抛出冲突异常。

    使用 LambdaQueryWrapper 同时匹配用户名和手机号
超过 0 时抛出 BusinessException(CONFLICT, "用户名或手机号已存在")
     */
    private void checkUserExists(String username, String phone) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
            .eq(User::getUsername, username)
            .or()
            .eq(User::getPhone, phone));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名或手机号已存在");
        }
    }

    /** Redis 滑动窗口限流：key 每小时最大请求次数 */
    private void checkRateLimit(String key, int maxPerHour) {
        Long count = redisUtils.increment(key);
        if (count != null && count == 1) {
            redisUtils.expire(key, Duration.ofHours(1));
        }
        if (count != null && count > maxPerHour) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "操作过于频繁，请稍后再试");
        }
    }

    /**
     * 构建登录成功后的返回对象，包含 accessToken、refreshToken、用户信息等。
     */
    private LoginVO buildLoginVO(User user, TokenService.TokenPair tokenPair) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setAvatar(user.getAvatar());
        userInfoVO.setPhone(user.getPhone());

        LoginVO vo = new LoginVO();
        vo.setAccessToken(tokenPair.accessToken());
        vo.setRefreshToken(tokenPair.refreshToken());
        vo.setTokenType("Bearer");
        vo.setExpiresIn(tokenPair.expiresIn());
        vo.setUserInfo(userInfoVO);
        vo.setRoleCode("user");
        return vo;
    }

    /**
     * 从 Authorization 头中提取 token 字符串，要求格式为 "Bearer {token}"，否则抛出异常。
     */
    private String extractToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Authorization格式错误");
        }
        return authorization.substring(7);
    }
}
