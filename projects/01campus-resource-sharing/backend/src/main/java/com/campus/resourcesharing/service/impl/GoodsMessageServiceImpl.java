package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.common.enums.MessageTypeEnum;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.query.PageQuery;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.dto.message.MessageSendDTO;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.entity.GoodsMessage;
import com.campus.resourcesharing.entity.SysUser;
import com.campus.resourcesharing.mapper.GoodsMessageMapper;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.service.GoodsMessageService;
import com.campus.resourcesharing.service.SysUserService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.message.MessageVO;
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
public class GoodsMessageServiceImpl extends ServiceImpl<GoodsMessageMapper, GoodsMessage> implements GoodsMessageService {

	private final GoodsInfoService goodsInfoService;
	private final SysUserService sysUserService;
	private final JwtUtil jwtUtil;

	public GoodsMessageServiceImpl(GoodsInfoService goodsInfoService,
								   SysUserService sysUserService,
								   JwtUtil jwtUtil) {
		this.goodsInfoService = goodsInfoService;
		this.sysUserService = sysUserService;
		this.jwtUtil = jwtUtil;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sendMessage(String token, MessageSendDTO dto) {
		Long senderId = getUserIdByToken(token);
		if (Objects.equals(senderId, dto.getReceiverId())) {
			throw new BusinessException(400, "不能给自己发送留言");
		}

		GoodsInfo goods = goodsInfoService.getById(dto.getGoodsId());
		if (goods == null || (goods.getDeleted() != null && goods.getDeleted() == 1)) {
			throw new BusinessException(404, "商品不存在");
		}

		Long ownerId = goods.getUserId();
		boolean senderIsOwner = Objects.equals(senderId, ownerId);
		boolean receiverIsOwner = Objects.equals(dto.getReceiverId(), ownerId);
		if (senderIsOwner == receiverIsOwner) {
			throw new BusinessException(400, "留言双方需包含商品发布者和咨询方");
		}

		String messageType = StringUtils.isBlank(dto.getMessageType()) ? MessageTypeEnum.CONSULT.getCode() : dto.getMessageType();
		GoodsMessage message = new GoodsMessage();
		message.setGoodsId(dto.getGoodsId());
		message.setSenderId(senderId);
		message.setReceiverId(dto.getReceiverId());
		message.setContent(dto.getContent().trim());
		message.setMessageType(messageType);
		message.setIsRead(0);
		message.setCreateTime(LocalDateTime.now());
		save(message);
	}

	@Override
	public PageResult<MessageVO> listMessages(String token, PageQuery query) {
		Long userId = getUserIdByToken(token);
		Page<GoodsMessage> page = query.toPage();
		LambdaQueryWrapper<GoodsMessage> wrapper = new LambdaQueryWrapper<GoodsMessage>()
				.and(w -> w.eq(GoodsMessage::getReceiverId, userId).or().eq(GoodsMessage::getSenderId, userId))
				.orderByDesc(GoodsMessage::getCreateTime);
		Page<GoodsMessage> result = this.page(page, wrapper);
		if (result.getRecords() == null || result.getRecords().isEmpty()) {
			return PageResult.empty(query.getPageNum(), query.getPageSize());
		}

		List<Long> goodsIds = result.getRecords().stream()
				.map(GoodsMessage::getGoodsId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
		List<Long> userIds = result.getRecords().stream()
				.flatMap(m -> java.util.stream.Stream.of(m.getSenderId(), m.getReceiverId()))
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());

		Map<Long, GoodsInfo> goodsMap = new HashMap<>();
		if (!goodsIds.isEmpty()) {
			List<GoodsInfo> goodsList = goodsInfoService.listByIds(goodsIds);
			for (GoodsInfo goods : goodsList) {
				goodsMap.put(goods.getId(), goods);
			}
		}

		Map<Long, SysUser> userMap = new HashMap<>();
		if (!userIds.isEmpty()) {
			List<SysUser> userList = sysUserService.listByIds(userIds);
			for (SysUser user : userList) {
				userMap.put(user.getId(), user);
			}
		}

		List<MessageVO> records = result.getRecords().stream().map(item -> {
			MessageVO vo = new MessageVO();
			vo.setId(item.getId());
			vo.setGoodsId(item.getGoodsId());
			GoodsInfo goods = item.getGoodsId() == null ? null : goodsMap.get(item.getGoodsId());
			vo.setGoodsTitle(goods == null ? null : goods.getTitle());
			vo.setGoodsMainImage(goods == null ? null : goods.getMainImage());

			vo.setSenderId(item.getSenderId());
			SysUser sender = userMap.get(item.getSenderId());
			vo.setSenderName(sender == null ? null : (StringUtils.isNotBlank(sender.getNickname()) ? sender.getNickname() : sender.getUsername()));

			vo.setReceiverId(item.getReceiverId());
			SysUser receiver = userMap.get(item.getReceiverId());
			vo.setReceiverName(receiver == null ? null : (StringUtils.isNotBlank(receiver.getNickname()) ? receiver.getNickname() : receiver.getUsername()));

			vo.setContent(item.getContent());
			vo.setMessageType(item.getMessageType());
			vo.setIsRead(item.getIsRead());
			vo.setMine(Objects.equals(item.getSenderId(), userId));
			vo.setCreateTime(item.getCreateTime());
			return vo;
		}).collect(Collectors.toList());

		return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void markAsRead(String token, Long id) {
		Long userId = getUserIdByToken(token);
		GoodsMessage message = getById(id);
		if (message == null) {
			throw new BusinessException(404, "消息不存在");
		}
		if (!Objects.equals(message.getReceiverId(), userId)) {
			throw new BusinessException(403, "无权限操作该消息");
		}
		if (message.getIsRead() != null && message.getIsRead() == 1) {
			return;
		}
		message.setIsRead(1);
		updateById(message);
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
