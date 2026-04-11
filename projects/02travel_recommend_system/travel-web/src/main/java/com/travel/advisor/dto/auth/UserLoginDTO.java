package com.travel.advisor.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录 DTO
 */
@Data
public class UserLoginDTO {

    /**
     * 登录方式标记。常见约定应为 username 或 phone。
     */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;

    private String username;
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "captchaId不能为空")
    private String captchaId;

    @NotBlank(message = "captchaCode不能为空")
    private String captchaCode;
}
