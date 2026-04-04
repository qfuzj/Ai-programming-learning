package com.travel.advisor.controller.admin;

import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.auth.AdminLoginDTO;
import com.travel.advisor.dto.auth.RefreshTokenDTO;
import com.travel.advisor.service.AdminAuthService;
import com.travel.advisor.vo.auth.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        return Result.success(adminAuthService.login(dto));
    }

    @PostMapping("/refresh-token")
    public Result<LoginVO> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        return Result.success(adminAuthService.refreshToken(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        adminAuthService.logout(authorization);
        return Result.success();
    }
}
