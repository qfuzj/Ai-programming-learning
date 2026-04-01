package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.dto.report.ReportAddDTO;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.entity.GoodsReport;
import com.campus.resourcesharing.mapper.GoodsReportMapper;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.service.GoodsReportService;
import com.campus.resourcesharing.service.SysUserService;
import com.campus.resourcesharing.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class GoodsReportServiceImpl extends ServiceImpl<GoodsReportMapper, GoodsReport> implements GoodsReportService {

	private final GoodsInfoService goodsInfoService;
	private final SysUserService sysUserService;
	private final JwtUtil jwtUtil;

	public GoodsReportServiceImpl(GoodsInfoService goodsInfoService,
								  SysUserService sysUserService,
								  JwtUtil jwtUtil) {
		this.goodsInfoService = goodsInfoService;
		this.sysUserService = sysUserService;
		this.jwtUtil = jwtUtil;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addReport(String token, ReportAddDTO dto) {
		Long userId = getUserIdByToken(token);
		if ("goods".equals(dto.getTargetType())) {
			GoodsInfo goods = goodsInfoService.getById(dto.getTargetId());
			if (goods == null || (goods.getDeleted() != null && goods.getDeleted() == 1)) {
				throw new BusinessException(404, "举报商品不存在");
			}
			if (Objects.equals(goods.getUserId(), userId)) {
				throw new BusinessException(400, "不能举报自己的商品");
			}
		} else if ("user".equals(dto.getTargetType())) {
			if (Objects.equals(dto.getTargetId(), userId)) {
				throw new BusinessException(400, "不能举报自己");
			}
			if (sysUserService.getById(dto.getTargetId()) == null) {
				throw new BusinessException(404, "举报用户不存在");
			}
		} else {
			throw new BusinessException(400, "举报类型不支持");
		}

		GoodsReport report = new GoodsReport();
		report.setReporterId(userId);
		report.setTargetType(dto.getTargetType());
		report.setTargetId(dto.getTargetId());
		report.setReason(dto.getReason());
		report.setDescription(dto.getDescription());
		report.setStatus("pending");
		report.setCreateTime(LocalDateTime.now());
		report.setUpdateTime(LocalDateTime.now());
		save(report);
	}

	private Long getUserIdByToken(String token) {
		Long userId = getUserIdNullable(token);
		if (userId == null) {
			throw new BusinessException(401, "登录已失效");
		}
		return userId;
	}

	private Long getUserIdNullable(String token) {
		if (StringUtils.isBlank(token)) {
			return null;
		}
		try {
			Claims claims = jwtUtil.parseToken(token);
			Object val = claims.get("userId");
			if (val instanceof Integer i) {
				return i.longValue();
			}
			if (val instanceof Long l) {
				return l;
			}
			if (val instanceof String s && StringUtils.isNotBlank(s)) {
				return Long.parseLong(s);
			}
			return null;
		} catch (Exception ex) {
			return null;
		}
	}
}
