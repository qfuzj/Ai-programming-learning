package com.travel.advisor.dto.user;

import lombok.Data;
import java.time.LocalDate;

/**
 * 用户个人信息更新的 DTO 对象，包含用户可以修改的基本信息。用于前端提交用户个人资料更新请求。
 */
@Data
public class UserProfileUpdateDTO {
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private String signature;
    private Long regionId;
}
