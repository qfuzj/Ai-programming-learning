package com.campus.resourcesharing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.resourcesharing.dto.report.ReportAddDTO;
import com.campus.resourcesharing.entity.GoodsReport;

public interface GoodsReportService extends IService<GoodsReport> {

	void addReport(String token, ReportAddDTO dto);
}
