package com.travel.advisor.service.impl;

import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.auth.CaptchaQueryDTO;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.service.CaptchaService;
import com.travel.advisor.utils.RedisUtils;
import com.travel.advisor.vo.auth.CaptchaVO;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CAPTCHA_PREFIX = "auth:captcha:";

    private final RedisUtils redisUtils;

    @Override
    public CaptchaVO createCaptcha(CaptchaQueryDTO dto) {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        specCaptcha.setCharType(SpecCaptcha.TYPE_DEFAULT);
        String code = specCaptcha.text().toLowerCase();

        String captchaId = UUID.randomUUID().toString();
        redisUtils.set(CAPTCHA_PREFIX + captchaId, code, Duration.ofMinutes(5));

        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaId(captchaId);
        vo.setCaptchaBase64(specCaptcha.toBase64());
        return vo;
    }

    @Override
    public void validateCaptcha(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码参数不能为空");
        }
        String key = CAPTCHA_PREFIX + captchaId;
        String storedCode = redisUtils.get(key);
        if (!StringUtils.hasText(storedCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码已过期");
        }
        storedCode = storedCode.trim();
        if (!storedCode.equalsIgnoreCase(captchaCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误");
        }
        redisUtils.delete(key);
    }
}
