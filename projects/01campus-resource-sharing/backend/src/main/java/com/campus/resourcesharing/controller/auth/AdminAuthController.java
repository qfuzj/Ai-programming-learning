package com.campus.resourcesharing.controller.auth;

import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.auth.LoginDTO;
import com.campus.resourcesharing.service.AuthService;
import com.campus.resourcesharing.vo.auth.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success("登录成功", authService.adminLogin(loginDTO));
    }
}
