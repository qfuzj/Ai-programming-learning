package com.campus.resourcesharing.vo.auth;

import lombok.Data;

@Data
public class CurrentUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
}
