package com.travel.advisor.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 刷新Token DTO
 */
@Data
public class RefreshTokenDTO {

    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
