package com.campus.resourcesharing.controller.user;

import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.user.UserPasswordDTO;
import com.campus.resourcesharing.dto.user.UserUpdateDTO;
import com.campus.resourcesharing.service.UserService;
import com.campus.resourcesharing.vo.user.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<UserProfileVO> profile(@RequestHeader("Authorization") String authorization) {
        return Result.success(userService.profile(extractToken(authorization)));
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestHeader("Authorization") String authorization,
                               @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateProfile(extractToken(authorization), dto);
        return Result.success("修改成功", null);
    }

    @PutMapping("/password")
    public Result<Void> password(@RequestHeader("Authorization") String authorization,
                                 @Valid @RequestBody UserPasswordDTO dto) {
        userService.changePassword(extractToken(authorization), dto);
        return Result.success("修改成功", null);
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestHeader("Authorization") String authorization,
                                                     @RequestParam("file") MultipartFile file) {
        String token = extractToken(authorization);
        return Result.success("上传成功", 
            Map.of("avatarUrl", userService.uploadAvatar(token, file)));
    }

    private String extractToken(String authorization) {
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }
}
