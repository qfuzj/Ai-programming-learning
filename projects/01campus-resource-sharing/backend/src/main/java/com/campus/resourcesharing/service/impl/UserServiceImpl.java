package com.campus.resourcesharing.service.impl;

import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.dto.user.UserPasswordDTO;
import com.campus.resourcesharing.dto.user.UserUpdateDTO;
import com.campus.resourcesharing.entity.SysUser;
import com.campus.resourcesharing.service.SysUserService;
import com.campus.resourcesharing.service.UserService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.user.UserProfileVO;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SysUserService sysUserService;

    public UserServiceImpl(JwtUtil jwtUtil, PasswordEncoder passwordEncoder, SysUserService sysUserService) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.sysUserService = sysUserService;
    }

    @Override
    public UserProfileVO profile(String token) {
        SysUser user = getUserByToken(token);
        return toProfile(user);
    }

    @Override
    public void updateProfile(String token, UserUpdateDTO dto) {
        SysUser user = getUserByToken(token);
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : user.getNickname());
        user.setPhone(dto.getPhone() != null ? dto.getPhone() : user.getPhone());
        user.setEmail(dto.getEmail() != null ? dto.getEmail() : user.getEmail());
        user.setGender(dto.getGender() != null ? dto.getGender() : user.getGender());
        user.setCollege(dto.getCollege() != null ? dto.getCollege() : user.getCollege());
        sysUserService.updateById(user);
    }

    @Override
    public void changePassword(String token, UserPasswordDTO dto) {
        SysUser user = getUserByToken(token);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserService.updateById(user);
    }

    @Override
    public String uploadAvatar(String token, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (!isImageFile(contentType)) {
            throw new BusinessException(400, "只支持图片文件");
        }

        try {
            SysUser user = getUserByToken(token);
            
            // 生成唯一的文件名
            String originalName = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf('.')).trim().replaceAll("\\s+", "");
                if (extension.isEmpty() || !extension.startsWith(".")) {
                    extension = ".jpg";
                }
            }
            String fileName = "avatar_" + user.getId() + "_" + UUID.randomUUID() + extension;
            
            // 保存到指定目录
            File dir = new File("upload" + File.separator + "avatar").getAbsoluteFile();
            if (!dir.exists() && !dir.mkdirs()) {
                throw new BusinessException(500, "创建上传目录失败");
            }
            
            File uploadFile = new File(dir, fileName).getAbsoluteFile();
            Files.copy(file.getInputStream(), uploadFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // 构建返回的 URL
            String avatarUrl = "/upload/avatar/" + fileName;
            
            // 更新用户头像
            user.setAvatar(avatarUrl);
            sysUserService.updateById(user);
            
            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessException(500, "文件上传失败");
        }
    }

    private boolean isImageFile(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.startsWith("image/");
    }

    private SysUser getUserByToken(String token) {
        Claims claims = jwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private UserProfileVO toProfile(SysUser user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        vo.setCollege(user.getCollege());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
