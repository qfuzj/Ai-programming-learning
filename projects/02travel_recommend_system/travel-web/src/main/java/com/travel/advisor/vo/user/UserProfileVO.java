package com.travel.advisor.vo.user;

import lombok.Data;
import java.time.LocalDate;

/**
 * 用户个人信息的 VO 对象，包含用户的基本信息和角色信息。用于前端展示用户个人资料页面。
 */
@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private String signature;
    private String role;
}
