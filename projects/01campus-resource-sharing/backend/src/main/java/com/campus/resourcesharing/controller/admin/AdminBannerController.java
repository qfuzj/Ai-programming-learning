package com.campus.resourcesharing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.admin.AdminBannerSaveDTO;
import com.campus.resourcesharing.entity.SysBanner;
import com.campus.resourcesharing.query.admin.AdminBannerPageQuery;
import com.campus.resourcesharing.service.SysBannerService;
import com.campus.resourcesharing.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/banner")
public class AdminBannerController extends AdminBaseController {

    private final SysBannerService sysBannerService;

    public AdminBannerController(JwtUtil jwtUtil, SysBannerService sysBannerService) {
        super(jwtUtil);
        this.sysBannerService = sysBannerService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysBanner>> page(@RequestHeader("Authorization") String authorization,
                                              @Valid AdminBannerPageQuery query) {
        assertAdmin(authorization);

        LambdaQueryWrapper<SysBanner> wrapper = new LambdaQueryWrapper<SysBanner>()
                .like(query.getKeyword() != null && !query.getKeyword().isBlank(), SysBanner::getTitle, query.getKeyword())
                .eq(query.getStatus() != null, SysBanner::getStatus, query.getStatus())
                .orderByAsc(SysBanner::getSort)
                .orderByDesc(SysBanner::getCreateTime);

        Page<SysBanner> page = sysBannerService.page(query.toPage(), wrapper);
        return Result.success(PageResult.from(page));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestHeader("Authorization") String authorization,
                            @Valid @RequestBody AdminBannerSaveDTO dto) {
        assertAdmin(authorization);

        SysBanner banner = new SysBanner();
        banner.setTitle(dto.getTitle() == null ? null : dto.getTitle().trim());
        banner.setImageUrl(dto.getImageUrl().trim());
        banner.setLinkUrl(dto.getLinkUrl() == null ? null : dto.getLinkUrl().trim());
        banner.setSort(dto.getSort());
        banner.setStatus(dto.getStatus());
        sysBannerService.save(banner);
        return Result.success("新增成功", null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestHeader("Authorization") String authorization,
                               @Valid @RequestBody AdminBannerSaveDTO dto) {
        assertAdmin(authorization);

        if (dto.getId() == null) {
            throw new BusinessException(400, "id不能为空");
        }

        SysBanner banner = sysBannerService.getById(dto.getId());
        if (banner == null) {
            throw new BusinessException(404, "轮播图不存在");
        }

        banner.setTitle(dto.getTitle() == null ? null : dto.getTitle().trim());
        banner.setImageUrl(dto.getImageUrl().trim());
        banner.setLinkUrl(dto.getLinkUrl() == null ? null : dto.getLinkUrl().trim());
        banner.setSort(dto.getSort());
        banner.setStatus(dto.getStatus());
        sysBannerService.updateById(banner);
        return Result.success("更新成功", null);
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@RequestHeader("Authorization") String authorization,
                                     @PathVariable Long id,
                                     @RequestParam Integer status) {
        assertAdmin(authorization);

        SysBanner banner = sysBannerService.getById(id);
        if (banner == null) {
            throw new BusinessException(404, "轮播图不存在");
        }

        banner.setStatus(status);
        sysBannerService.updateById(banner);
        return Result.success("操作成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id) {
        assertAdmin(authorization);
        sysBannerService.removeById(id);
        return Result.success("删除成功", null);
    }
}
