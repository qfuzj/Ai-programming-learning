package com.travel.advisor.service;

import com.travel.advisor.dto.auth.CaptchaQueryDTO;
import com.travel.advisor.vo.auth.CaptchaVO;

public interface CaptchaService {

    CaptchaVO createCaptcha(CaptchaQueryDTO dto);

    void validateCaptcha(String captchaId, String captchaCode);
}
