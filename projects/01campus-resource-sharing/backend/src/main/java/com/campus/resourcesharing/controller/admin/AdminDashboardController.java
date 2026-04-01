package com.campus.resourcesharing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.entity.GoodsOrder;
import com.campus.resourcesharing.entity.GoodsReport;
import com.campus.resourcesharing.entity.SysUser;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.service.GoodsOrderService;
import com.campus.resourcesharing.service.GoodsReportService;
import com.campus.resourcesharing.service.SysUserService;
import com.campus.resourcesharing.utils.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController extends AdminBaseController {

    private final SysUserService sysUserService;
    private final GoodsInfoService goodsInfoService;
    private final GoodsOrderService goodsOrderService;
    private final GoodsReportService goodsReportService;

    public AdminDashboardController(JwtUtil jwtUtil,
                                    SysUserService sysUserService,
                                    GoodsInfoService goodsInfoService,
                                    GoodsOrderService goodsOrderService,
                                    GoodsReportService goodsReportService) {
        super(jwtUtil);
        this.sysUserService = sysUserService;
        this.goodsInfoService = goodsInfoService;
        this.goodsOrderService = goodsOrderService;
        this.goodsReportService = goodsReportService;
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> stats(@RequestHeader("Authorization") String authorization) {
        assertAdmin(authorization);

        long userCount = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0));
        long goodsCount = goodsInfoService.count(new LambdaQueryWrapper<GoodsInfo>()
                .eq(GoodsInfo::getDeleted, 0));
        long orderCount = goodsOrderService.count();
        long pendingReportCount = goodsReportService.count(new LambdaQueryWrapper<GoodsReport>()
                .eq(GoodsReport::getStatus, "pending"));

        return Result.success(Map.of(
                "userCount", userCount,
                "goodsCount", goodsCount,
                "orderCount", orderCount,
                "pendingReportCount", pendingReportCount
        ));
    }
}
