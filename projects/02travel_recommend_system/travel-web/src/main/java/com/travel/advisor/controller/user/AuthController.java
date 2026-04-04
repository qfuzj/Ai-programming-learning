package com.travel.advisor.controller.user;

import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.auth.RefreshTokenDTO;
import com.travel.advisor.dto.auth.ResetPasswordDTO;
import com.travel.advisor.dto.auth.UserLoginDTO;
import com.travel.advisor.dto.auth.UserRegisterDTO;
import com.travel.advisor.service.AuthService;
import com.travel.advisor.vo.auth.LoginVO;
import com.travel.advisor.vo.auth.RegisterVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<RegisterVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/refresh-token")
    public Result<LoginVO> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        return Result.success(authService.refreshToken(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return Result.success();
    }
}
