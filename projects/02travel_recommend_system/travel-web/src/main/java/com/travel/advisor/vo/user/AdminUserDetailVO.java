package com.travel.advisor.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理员用户详情 VO
 */
@Data
public class AdminUserDetailVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private Long regionId;
    private String regionName;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}