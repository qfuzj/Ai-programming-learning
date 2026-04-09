package com.travel.advisor.controller.admin;

import com.travel.advisor.annotation.OperationLog;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.user.UserQueryDTO;
import com.travel.advisor.dto.user.UserStatusDTO;
import com.travel.advisor.service.AdminUserManageService;
import com.travel.advisor.vo.user.AdminUserDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserManageService adminUserManageService;

    @GetMapping
    public Result<PageResult<AdminUserDetailVO>> page(@Valid UserQueryDTO queryDTO) {
        return Result.success(adminUserManageService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<AdminUserDetailVO> detail(@PathVariable Long id) {
        return Result.success(adminUserManageService.detail(id));
    }

    @PutMapping("/{id}/status")
    @OperationLog(module = "user", action = "update", description = "更新用户状态")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO dto) {
        adminUserManageService.updateStatus(id, dto.getStatus());
        return Result.success();
    }
}