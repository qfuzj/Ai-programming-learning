package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.resourcesharing.common.enums.UserRoleEnum;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.dto.auth.LoginDTO;
import com.campus.resourcesharing.dto.auth.RegisterDTO;
import com.campus.resourcesharing.entity.SysUser;
import com.campus.resourcesharing.service.AuthService;
import com.campus.resourcesharing.service.SysUserService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.auth.CurrentUserVO;
import com.campus.resourcesharing.vo.auth.LoginVO;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(SysUserService sysUserService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        SysUser existed = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, registerDTO.getUsername()));
        if (existed != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(UserRoleEnum.USER.getCode());
        user.setStatus(1);
        user.setDeleted(0);
        sysUserService.save(user);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        return loginInternal(loginDTO, false);
    }

    @Override
    public LoginVO adminLogin(LoginDTO loginDTO) {
        return loginInternal(loginDTO, true);
    }

    @Override
    public CurrentUserVO currentUser(String token) {
        Claims claims = jwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toCurrentUser(user);
    }

    @Override
    public void logout() {
        // JWT 无状态，前端删除 token 即可。预留扩展黑名单逻辑。
    }

    private LoginVO loginInternal(LoginDTO loginDTO, boolean adminOnly) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (adminOnly && !UserRoleEnum.ADMIN.getCode().equals(user.getRole())) {
            throw new BusinessException(403, "无管理员权限");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        String token = jwtUtil.generateToken(claims, user.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(toCurrentUser(user));
        return loginVO;
    }

    private CurrentUserVO toCurrentUser(SysUser user) {
        CurrentUserVO vo = new CurrentUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }
}
