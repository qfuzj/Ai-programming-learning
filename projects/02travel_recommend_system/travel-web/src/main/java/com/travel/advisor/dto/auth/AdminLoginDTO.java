package com.travel.advisor.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "captchaId不能为空")
    private String captchaId;

    @NotBlank(message = "captchaCode不能为空")
    private String captchaCode;
}
