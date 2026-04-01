package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.auth.AdminLoginDTO;
import com.travel.advisor.dto.auth.RefreshTokenDTO;
import com.travel.advisor.entity.AdminUser;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.AdminUserMapper;
import com.travel.advisor.security.LoginUser;
import com.travel.advisor.security.TokenService;
import com.travel.advisor.service.AdminAuthService;
import com.travel.advisor.service.CaptchaService;
import com.travel.advisor.vo.auth.AdminInfoVO;
import com.travel.advisor.vo.auth.LoginVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;
    private final TokenService tokenService;

    @Override
    public LoginVO login(AdminLoginDTO dto) {
        // 管理员登录同样要求验证码先通过，降低密码暴力尝试风险。
        captchaService.validateCaptcha(dto.getCaptchaId(), dto.getCaptchaCode());
        AdminUser adminUser = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
            .eq(AdminUser::getUsername, dto.getUsername())
            .last("limit 1"));
        if (adminUser == null || !passwordEncoder.matches(dto.getPassword(), adminUser.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (adminUser.getStatus() != null && adminUser.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前管理员已被禁用");
        }

        adminUser.setLastLoginTime(LocalDateTime.now());
        adminUserMapper.updateById(adminUser);

        // 管理员 token 的 roleType 固定为 ADMIN，供权限系统识别。
        TokenService.TokenPair tokenPair = tokenService.createTokenPair(LoginUser.builder()
            .userId(adminUser.getId())
            .username(adminUser.getUsername())
            .roleType("ADMIN")
            .roleCode(adminUser.getRole())
            .loginType("username")
            .build());

        return buildLoginVO(adminUser, tokenPair);
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
        // refreshToken 只能刷新同角色 token，防止用户 token 冒充管理员。
        if (!"ADMIN".equalsIgnoreCase(loginUser.getRoleType())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "非管理员token");
        }
        if (!tokenService.verifyRefreshToken(loginUser, claims.getId(), dto.getRefreshToken())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "refreshToken已失效");
        }

        AdminUser adminUser = adminUserMapper.selectById(loginUser.getUserId());
        if (adminUser == null || (adminUser.getStatus() != null && adminUser.getStatus() == 0)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "管理员不存在或已禁用");
        }

        TokenService.TokenPair tokenPair = tokenService.createTokenPair(LoginUser.builder()
            .userId(adminUser.getId())
            .username(adminUser.getUsername())
            .roleType("ADMIN")
            .roleCode(adminUser.getRole())
            .loginType("username")
            .build());
        // 轮换刷新：新 token 生效后，旧 refreshToken 立即失效。
        tokenService.invalidateToken(loginUser, claims.getId(), 0);
        return buildLoginVO(adminUser, tokenPair);
    }

    @Override
    public void logout(String authorization) {
        String token = extractToken(authorization);
        Claims claims = tokenService.parseClaims(token);
        LoginUser loginUser = tokenService.parseLoginUser(token);
        if (!"ADMIN".equalsIgnoreCase(loginUser.getRoleType())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "非管理员token");
        }
        // 退出时把当前 accessToken 加入黑名单，避免其在剩余有效期内继续使用。
        long remaining = Math.max(0, claims.getExpiration().toInstant().getEpochSecond() - Instant.now().getEpochSecond());
        tokenService.invalidateToken(loginUser, claims.getId(), remaining);
    }

    private LoginVO buildLoginVO(AdminUser adminUser, TokenService.TokenPair tokenPair) {
        AdminInfoVO adminInfoVO = new AdminInfoVO();
        adminInfoVO.setAdminId(adminUser.getId());
        adminInfoVO.setUsername(adminUser.getUsername());
        adminInfoVO.setRealName(adminUser.getRealName());
        adminInfoVO.setAvatar(adminUser.getAvatar());

        LoginVO vo = new LoginVO();
        vo.setAccessToken(tokenPair.accessToken());
        vo.setRefreshToken(tokenPair.refreshToken());
        vo.setTokenType("Bearer");
        vo.setExpiresIn(tokenPair.expiresIn());
        vo.setAdminInfo(adminInfoVO);
        vo.setRoleCode(adminUser.getRole());
        return vo;
    }

    private String extractToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Authorization格式错误");
        }
        return authorization.substring(7);
    }
}
