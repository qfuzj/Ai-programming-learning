package com.campus.resourcesharing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.entity.SysUser;
import com.campus.resourcesharing.query.admin.AdminUserPageQuery;
import com.campus.resourcesharing.service.SysUserService;
import com.campus.resourcesharing.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController extends AdminBaseController {

    private final SysUserService sysUserService;

    public AdminUserController(JwtUtil jwtUtil, SysUserService sysUserService) {
        super(jwtUtil);
        this.sysUserService = sysUserService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(@RequestHeader("Authorization") String authorization,
                                            @Valid AdminUserPageQuery query) {
        assertAdmin(authorization);

        String keyword = query.getKeyword() == null ? null : query.getKeyword().trim();
        boolean hasKeyword = keyword != null && !keyword.isEmpty();

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(query.getRole() != null && !query.getRole().isBlank(), SysUser::getRole, query.getRole())
                .orderByDesc(SysUser::getCreateTime);

        if (hasKeyword) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getNickname, keyword));
        }

        Page<SysUser> page = sysUserService.page(query.toPage(), wrapper);
        return Result.success(PageResult.from(page));
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@RequestHeader("Authorization") String authorization,
                                     @PathVariable Long id,
                                     @RequestParam Integer status) {
        assertAdmin(authorization);

        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值非法");
        }

        SysUser user = sysUserService.getById(id);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setStatus(status);
        sysUserService.updateById(user);
        return Result.success("操作成功", null);
    }
}
