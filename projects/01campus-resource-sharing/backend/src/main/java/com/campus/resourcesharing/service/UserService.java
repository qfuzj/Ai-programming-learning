package com.campus.resourcesharing.service;

import com.campus.resourcesharing.dto.user.UserPasswordDTO;
import com.campus.resourcesharing.dto.user.UserUpdateDTO;
import com.campus.resourcesharing.vo.user.UserProfileVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserProfileVO profile(String token);

    void updateProfile(String token, UserUpdateDTO dto);

    void changePassword(String token, UserPasswordDTO dto);

    String uploadAvatar(String token, MultipartFile file);
}
