package com.campus.resourcesharing.controller.auth;

import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.auth.LoginDTO;
import com.campus.resourcesharing.dto.auth.RegisterDTO;
import com.campus.resourcesharing.service.AuthService;
import com.campus.resourcesharing.vo.auth.CurrentUserVO;
import com.campus.resourcesharing.vo.auth.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success("登录成功", authService.login(loginDTO));
    }

    @GetMapping("/currentUser")
    public Result<CurrentUserVO> currentUser(@RequestHeader("Authorization") String authorization) {
        return Result.success(authService.currentUser(extractToken(authorization)));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("退出成功", null);
    }

    private String extractToken(String authorization) {
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }
}
