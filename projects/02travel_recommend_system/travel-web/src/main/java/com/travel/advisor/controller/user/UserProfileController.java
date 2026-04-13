package com.travel.advisor.controller.user;

import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.user.UserPreferenceTagsUpdateDTO;
import com.travel.advisor.dto.user.UserProfileUpdateDTO;
import com.travel.advisor.service.UserProfileService;
import com.travel.advisor.vo.user.UserProfilePortraitVO;
import com.travel.advisor.vo.user.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.travel.advisor.entity.Tag;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public Result<UserProfileVO> getMyProfile() {
        return Result.success(userProfileService.getMyProfile());
    }

    @PutMapping("/me")
    public Result<Void> updateMyProfile(@RequestBody UserProfileUpdateDTO dto) {
        userProfileService.updateMyProfile(dto);
        return Result.success();
    }

    @GetMapping("/portrait")
    public Result<UserProfilePortraitVO> getMyPortrait() {
        return Result.success(userProfileService.getMyPortrait());
    }

    @GetMapping("/preference-tags")
    public Result<List<Tag>> getMyPreferenceTags() {
        return Result.success(userProfileService.getMyPreferenceTags());
    }

    @PutMapping("/preference-tags")
    public Result<Void> updatePreferenceTags(@RequestBody UserPreferenceTagsUpdateDTO dto) {
        userProfileService.updatePreferenceTags(dto);
        return Result.success();
    }
}
