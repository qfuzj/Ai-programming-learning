package com.campus.resourcesharing.controller.user;

import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.report.ReportAddDTO;
import com.campus.resourcesharing.service.GoodsReportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final GoodsReportService goodsReportService;

    public ReportController(GoodsReportService goodsReportService) {
        this.goodsReportService = goodsReportService;
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestHeader("Authorization") String authorization,
                            @Valid @RequestBody ReportAddDTO dto) {
        goodsReportService.addReport(extractToken(authorization), dto);
        return Result.success("举报成功", null);
    }

    private String extractToken(String authorization) {
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }
}
