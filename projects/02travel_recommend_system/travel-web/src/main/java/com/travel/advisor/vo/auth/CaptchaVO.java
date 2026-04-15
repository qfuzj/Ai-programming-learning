package com.travel.advisor.vo.auth;

import lombok.Data;

/**
 * 验证码VO
 */
@Data
public class CaptchaVO {

    /**
     * 验证码ID
     */
    private String captchaId;
    /**
     * 验证码图片的Base64编码字符串
     */
    private String captchaBase64;
}
