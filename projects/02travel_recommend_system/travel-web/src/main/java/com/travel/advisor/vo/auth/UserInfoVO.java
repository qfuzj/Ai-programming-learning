package com.travel.advisor.vo.auth;

import lombok.Data;

@Data
public class UserInfoVO {

    private Long userId;
    private String username;
    private String avatar;
    private String phone;
}
