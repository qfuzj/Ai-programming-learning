package com.travel.advisor.service;

import com.travel.advisor.dto.auth.RefreshTokenDTO;
import com.travel.advisor.dto.auth.ResetPasswordDTO;
import com.travel.advisor.dto.auth.UserLoginDTO;
import com.travel.advisor.dto.auth.UserRegisterDTO;
import com.travel.advisor.vo.auth.LoginVO;
import com.travel.advisor.vo.auth.RegisterVO;

public interface AuthService {

    RegisterVO register(UserRegisterDTO dto);

    LoginVO login(UserLoginDTO dto);

    LoginVO refreshToken(RefreshTokenDTO dto);

    void logout(String authorization);

    void resetPassword(ResetPasswordDTO dto);
}
