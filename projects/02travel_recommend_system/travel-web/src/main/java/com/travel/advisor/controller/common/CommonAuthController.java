package com.travel.advisor.controller.common;

import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.auth.CaptchaQueryDTO;
import com.travel.advisor.service.CaptchaService;
import com.travel.advisor.vo.auth.CaptchaVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common/auth")
@RequiredArgsConstructor
public class CommonAuthController {

    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha(@Valid @ModelAttribute CaptchaQueryDTO dto) {
        return Result.success(captchaService.createCaptcha(dto));
    }
}
