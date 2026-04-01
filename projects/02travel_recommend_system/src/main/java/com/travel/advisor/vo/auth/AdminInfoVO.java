package com.travel.advisor.vo.auth;

import lombok.Data;

@Data
public class AdminInfoVO {

    private Long adminId;
    private String username;
    private String realName;
    private String avatar;
}
