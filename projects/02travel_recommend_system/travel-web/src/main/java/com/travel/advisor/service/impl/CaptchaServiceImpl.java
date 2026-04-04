package com.travel.advisor.service.impl;

import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.auth.CaptchaQueryDTO;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.service.CaptchaService;
import com.travel.advisor.utils.RedisUtils;
import com.travel.advisor.vo.auth.CaptchaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CAPTCHA_PREFIX = "auth:captcha:";
    private static final Random RANDOM = new Random();

    private final RedisUtils redisUtils;

    @Override
    public CaptchaVO createCaptcha(CaptchaQueryDTO dto) {
        String code = String.format(Locale.ROOT, "%04d", RANDOM.nextInt(10000));
        String captchaId = UUID.randomUUID().toString();
        redisUtils.set(CAPTCHA_PREFIX + captchaId, code, Duration.ofMinutes(5));

        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaId(captchaId);
        vo.setCaptchaBase64(Base64.getEncoder().encodeToString(code.getBytes(StandardCharsets.UTF_8)));
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
