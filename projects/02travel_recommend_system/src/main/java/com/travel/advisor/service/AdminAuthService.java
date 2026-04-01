package com.travel.advisor.service;

import com.travel.advisor.dto.auth.AdminLoginDTO;
import com.travel.advisor.dto.auth.RefreshTokenDTO;
import com.travel.advisor.vo.auth.LoginVO;

public interface AdminAuthService {

    LoginVO login(AdminLoginDTO dto);

    LoginVO refreshToken(RefreshTokenDTO dto);

    void logout(String authorization);
}
