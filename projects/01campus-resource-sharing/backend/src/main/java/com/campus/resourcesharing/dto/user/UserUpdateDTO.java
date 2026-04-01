package com.campus.resourcesharing.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "邮箱格式不正确")
    private String email;

    private Integer gender;

    @Size(max = 100, message = "学院长度不能超过 100")
    private String college;
}
