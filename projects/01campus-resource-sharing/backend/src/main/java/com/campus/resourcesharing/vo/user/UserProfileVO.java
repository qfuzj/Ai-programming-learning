package com.campus.resourcesharing.vo.user;

import lombok.Data;

@Data
public class UserProfileVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private String college;
    private String role;
    private Integer status;
}
