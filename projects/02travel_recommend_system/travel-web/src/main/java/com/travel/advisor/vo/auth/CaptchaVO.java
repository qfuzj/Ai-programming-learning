package com.travel.advisor.vo.auth;

import lombok.Data;

@Data
public class CaptchaVO {

    private String captchaId;
    private String captchaBase64;
}
