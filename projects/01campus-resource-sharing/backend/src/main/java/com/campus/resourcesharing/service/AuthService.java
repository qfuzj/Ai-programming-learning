package com.campus.resourcesharing.service;

import com.campus.resourcesharing.dto.auth.LoginDTO;
import com.campus.resourcesharing.dto.auth.RegisterDTO;
import com.campus.resourcesharing.vo.auth.CurrentUserVO;
import com.campus.resourcesharing.vo.auth.LoginVO;

public interface AuthService {

    void register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    LoginVO adminLogin(LoginDTO loginDTO);

    CurrentUserVO currentUser(String token);

    void logout();
}
