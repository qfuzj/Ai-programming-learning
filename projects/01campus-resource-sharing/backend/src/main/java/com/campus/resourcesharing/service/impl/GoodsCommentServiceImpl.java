package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.common.enums.OrderStatusEnum;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.dto.comment.CommentAddDTO;
import com.campus.resourcesharing.entity.GoodsComment;
import com.campus.resourcesharing.entity.GoodsOrder;
import com.campus.resourcesharing.entity.SysUser;
import com.campus.resourcesharing.mapper.GoodsCommentMapper;
import com.campus.resourcesharing.service.GoodsOrderService;
import com.campus.resourcesharing.service.GoodsCommentService;
import com.campus.resourcesharing.service.SysUserService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.comment.CommentVO;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GoodsCommentServiceImpl extends ServiceImpl<GoodsCommentMapper, GoodsComment> implements GoodsCommentService {

	private final GoodsOrderService goodsOrderService;
	private final SysUserService sysUserService;
	private final JwtUtil jwtUtil;

	public GoodsCommentServiceImpl(GoodsOrderService goodsOrderService,
								   SysUserService sysUserService,
								   JwtUtil jwtUtil) {
		this.goodsOrderService = goodsOrderService;
		this.sysUserService = sysUserService;
		this.jwtUtil = jwtUtil;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addComment(String token, CommentAddDTO dto) {
		Long userId = getUserIdByToken(token);
		GoodsOrder order = goodsOrderService.getById(dto.getOrderId());
		if (order == null) {
			throw new BusinessException(404, "订单不存在");
		}
		if (!Objects.equals(order.getGoodsId(), dto.getGoodsId())) {
			throw new BusinessException(400, "订单与商品不匹配");
		}
		if (!OrderStatusEnum.COMPLETED.getCode().equals(order.getStatus())) {
			throw new BusinessException(400, "仅已完成订单可评价");
		}
		if (!Objects.equals(userId, order.getBuyerId()) && !Objects.equals(userId, order.getSellerId())) {
			throw new BusinessException(403, "无权限评价该订单");
		}

		Long expectedToUserId = Objects.equals(userId, order.getBuyerId()) ? order.getSellerId() : order.getBuyerId();
		if (!Objects.equals(dto.getToUserId(), expectedToUserId)) {
			throw new BusinessException(400, "被评价用户不正确");
		}

		long duplicate = count(new LambdaQueryWrapper<GoodsComment>()
				.eq(GoodsComment::getOrderId, dto.getOrderId())
				.eq(GoodsComment::getFromUserId, userId));
		if (duplicate > 0) {
			throw new BusinessException(400, "该订单已评价，请勿重复提交");
		}

		GoodsComment comment = new GoodsComment();
		comment.setOrderId(dto.getOrderId());
		comment.setGoodsId(dto.getGoodsId());
		comment.setFromUserId(userId);
		comment.setToUserId(dto.getToUserId());
		comment.setScore(dto.getScore());
		comment.setContent(dto.getContent());
		comment.setCreateTime(LocalDateTime.now());
		save(comment);
	}

	@Override
	public List<CommentVO> listByGoods(Long goodsId) {
		List<GoodsComment> comments = list(new LambdaQueryWrapper<GoodsComment>()
				.eq(GoodsComment::getGoodsId, goodsId)
				.orderByDesc(GoodsComment::getCreateTime));
		if (comments == null || comments.isEmpty()) {
			return List.of();
		}

		List<Long> userIds = comments.stream()
				.flatMap(c -> java.util.stream.Stream.of(c.getFromUserId(), c.getToUserId()))
				.distinct()
				.collect(Collectors.toList());
		List<SysUser> users = sysUserService.listByIds(userIds);
		Map<Long, SysUser> userMap = new HashMap<>();
		for (SysUser user : users) {
			userMap.put(user.getId(), user);
		}

		return comments.stream().map(item -> {
			CommentVO vo = new CommentVO();
			vo.setId(item.getId());
			vo.setOrderId(item.getOrderId());
			vo.setGoodsId(item.getGoodsId());
			vo.setFromUserId(item.getFromUserId());
			vo.setToUserId(item.getToUserId());
			vo.setScore(item.getScore());
			vo.setContent(item.getContent());
			vo.setCreateTime(item.getCreateTime());

			SysUser from = userMap.get(item.getFromUserId());
			vo.setFromUserName(from == null ? null : (StringUtils.isNotBlank(from.getNickname()) ? from.getNickname() : from.getUsername()));
			SysUser to = userMap.get(item.getToUserId());
			vo.setToUserName(to == null ? null : (StringUtils.isNotBlank(to.getNickname()) ? to.getNickname() : to.getUsername()));
			return vo;
		}).collect(Collectors.toList());
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
