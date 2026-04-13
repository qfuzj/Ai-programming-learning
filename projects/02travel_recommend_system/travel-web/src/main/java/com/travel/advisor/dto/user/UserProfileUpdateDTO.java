package com.travel.advisor.dto.user;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileUpdateDTO {
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private String signature;
}
