package com.travel.advisor.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CaptchaQueryDTO {

    @NotBlank(message = "bizType不能为空")
    private String bizType;

    private String phone;
}
