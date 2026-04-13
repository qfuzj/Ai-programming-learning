package com.travel.advisor.vo.user;

import lombok.Data;
import java.time.LocalDate;

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
