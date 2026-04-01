package com.campus.resourcesharing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.admin.AdminNoticeSaveDTO;
import com.campus.resourcesharing.entity.SysNotice;
import com.campus.resourcesharing.query.admin.AdminNoticePageQuery;
import com.campus.resourcesharing.service.SysNoticeService;
import com.campus.resourcesharing.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notice")
public class AdminNoticeController extends AdminBaseController {

    private final SysNoticeService sysNoticeService;

    public AdminNoticeController(JwtUtil jwtUtil, SysNoticeService sysNoticeService) {
        super(jwtUtil);
        this.sysNoticeService = sysNoticeService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysNotice>> page(@RequestHeader("Authorization") String authorization,
                                              @Valid AdminNoticePageQuery query) {
        assertAdmin(authorization);

        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<SysNotice>()
                .like(query.getKeyword() != null && !query.getKeyword().isBlank(), SysNotice::getTitle, query.getKeyword())
                .eq(query.getStatus() != null, SysNotice::getStatus, query.getStatus())
                .orderByDesc(SysNotice::getCreateTime);

        Page<SysNotice> page = sysNoticeService.page(query.toPage(), wrapper);
        return Result.success(PageResult.from(page));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestHeader("Authorization") String authorization,
                            @Valid @RequestBody AdminNoticeSaveDTO dto) {
        assertAdmin(authorization);

        SysNotice notice = new SysNotice();
        notice.setTitle(dto.getTitle().trim());
        notice.setContent(dto.getContent().trim());
        notice.setStatus(dto.getStatus());
        sysNoticeService.save(notice);
        return Result.success("新增成功", null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestHeader("Authorization") String authorization,
                               @Valid @RequestBody AdminNoticeSaveDTO dto) {
        assertAdmin(authorization);

        if (dto.getId() == null) {
            throw new BusinessException(400, "id不能为空");
        }

        SysNotice notice = sysNoticeService.getById(dto.getId());
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }

        notice.setTitle(dto.getTitle().trim());
        notice.setContent(dto.getContent().trim());
        notice.setStatus(dto.getStatus());
        sysNoticeService.updateById(notice);
        return Result.success("更新成功", null);
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@RequestHeader("Authorization") String authorization,
                                     @PathVariable Long id,
                                     @RequestParam Integer status) {
        assertAdmin(authorization);

        SysNotice notice = sysNoticeService.getById(id);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        notice.setStatus(status);
        sysNoticeService.updateById(notice);
        return Result.success("操作成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id) {
        assertAdmin(authorization);
        sysNoticeService.removeById(id);
        return Result.success("删除成功", null);
    }
}
