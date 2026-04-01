package com.campus.resourcesharing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.admin.AdminReportReviewDTO;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.entity.GoodsReport;
import com.campus.resourcesharing.query.admin.AdminReportPageQuery;
import com.campus.resourcesharing.service.admin.AdminUserLookupService;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.service.GoodsReportService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.admin.AdminReportVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/report")
public class AdminReportController extends AdminBaseController {

    private final GoodsReportService goodsReportService;
    private final GoodsInfoService goodsInfoService;
    private final AdminUserLookupService adminUserLookupService;

    public AdminReportController(JwtUtil jwtUtil,
                                 GoodsReportService goodsReportService,
                                 GoodsInfoService goodsInfoService,
                                 AdminUserLookupService adminUserLookupService) {
        super(jwtUtil);
        this.goodsReportService = goodsReportService;
        this.goodsInfoService = goodsInfoService;
        this.adminUserLookupService = adminUserLookupService;
    }

    @GetMapping("/page")
    public Result<PageResult<AdminReportVO>> page(@RequestHeader("Authorization") String authorization,
                                                  @Valid AdminReportPageQuery query) {
        assertAdmin(authorization);

        boolean hasReporterKeyword = query.getReporterKeyword() != null && !query.getReporterKeyword().isBlank();
        Set<Long> reporterIds = adminUserLookupService.findUserIdsByKeyword(query.getReporterKeyword());
        if (hasReporterKeyword && reporterIds.isEmpty()) {
            return Result.success(PageResult.empty(query.getPageNum(), query.getPageSize()));
        }

        LambdaQueryWrapper<GoodsReport> wrapper = new LambdaQueryWrapper<GoodsReport>()
                .eq(query.getTargetType() != null && !query.getTargetType().isBlank(), GoodsReport::getTargetType, query.getTargetType())
                .eq(query.getStatus() != null && !query.getStatus().isBlank(), GoodsReport::getStatus, query.getStatus())
                .eq(query.getReporterId() != null, GoodsReport::getReporterId, query.getReporterId())
                .in(hasReporterKeyword, GoodsReport::getReporterId, reporterIds)
                .orderByDesc(GoodsReport::getCreateTime);

        Page<GoodsReport> page = goodsReportService.page(query.toPage(), wrapper);
        Map<Long, String> userNameMap = adminUserLookupService.buildUserDisplayNameMap(
                page.getRecords().stream().map(GoodsReport::getReporterId).collect(Collectors.toSet())
        );

        List<AdminReportVO> records = page.getRecords().stream().map(item -> {
            AdminReportVO vo = new AdminReportVO();
            vo.setId(item.getId());
            vo.setReporterId(item.getReporterId());
            vo.setReporterName(userNameMap.get(item.getReporterId()));
            vo.setTargetType(item.getTargetType());
            vo.setTargetId(item.getTargetId());
            vo.setReason(item.getReason());
            vo.setDescription(item.getDescription());
            vo.setStatus(item.getStatus());
            vo.setHandleResult(item.getHandleResult());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).toList();

        return Result.success(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), records));
    }

    @PutMapping("/review/{id}")
    public Result<Void> review(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id,
                               @Valid @RequestBody AdminReportReviewDTO dto) {
        assertAdmin(authorization);

        String status = dto.getStatus().trim();
        if (!"approved".equals(status) && !"rejected".equals(status)) {
            throw new BusinessException(400, "审核状态非法");
        }

        GoodsReport report = goodsReportService.getById(id);
        if (report == null) {
            throw new BusinessException(404, "举报不存在");
        }

        report.setStatus(status);
        report.setHandleResult(dto.getHandleResult() == null ? null : dto.getHandleResult().trim());
        goodsReportService.updateById(report);

        if ("approved".equals(status) && "goods".equals(report.getTargetType()) && report.getTargetId() != null) {
            GoodsInfo goods = goodsInfoService.getById(report.getTargetId());
            if (goods != null && (goods.getDeleted() == null || goods.getDeleted() == 0)) {
                goods.setStatus("off_shelf");
                goodsInfoService.updateById(goods);
            }
        }

        return Result.success("审核成功", null);
    }
}
