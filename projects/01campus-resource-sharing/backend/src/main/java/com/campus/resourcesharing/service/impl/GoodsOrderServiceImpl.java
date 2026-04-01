package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.common.enums.GoodsStatusEnum;
import com.campus.resourcesharing.common.enums.OrderStatusEnum;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.query.PageQuery;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.dto.order.OrderCreateDTO;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.entity.GoodsOrder;
import com.campus.resourcesharing.entity.GoodsOrderLog;
import com.campus.resourcesharing.entity.SysUser;
import com.campus.resourcesharing.mapper.GoodsOrderMapper;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.service.GoodsOrderLogService;
import com.campus.resourcesharing.service.GoodsOrderService;
import com.campus.resourcesharing.service.SysUserService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.order.OrderDetailVO;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GoodsOrderServiceImpl extends ServiceImpl<GoodsOrderMapper, GoodsOrder> implements GoodsOrderService {

	private final GoodsInfoService goodsInfoService;
	private final GoodsOrderLogService goodsOrderLogService;
	private final SysUserService sysUserService;
	private final JwtUtil jwtUtil;

	public GoodsOrderServiceImpl(GoodsInfoService goodsInfoService,
								 GoodsOrderLogService goodsOrderLogService,
								 SysUserService sysUserService,
								 JwtUtil jwtUtil) {
		this.goodsInfoService = goodsInfoService;
		this.goodsOrderLogService = goodsOrderLogService;
		this.sysUserService = sysUserService;
		this.jwtUtil = jwtUtil;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long createOrder(String token, OrderCreateDTO dto) {
		Long buyerId = getUserIdByToken(token);
		GoodsInfo goods = goodsInfoService.getById(dto.getGoodsId());
		if (goods == null || (goods.getDeleted() != null && goods.getDeleted() == 1)) {
			throw new BusinessException(404, "商品不存在");
		}
		if (Objects.equals(goods.getUserId(), buyerId)) {
			throw new BusinessException(400, "不能购买自己的商品");
		}
		if (!GoodsStatusEnum.ON_SALE.getCode().equals(goods.getStatus())) {
			throw new BusinessException(400, "商品当前不可下单");
		}

		long activeCount = count(new LambdaQueryWrapper<GoodsOrder>()
				.eq(GoodsOrder::getGoodsId, goods.getId())
				.in(GoodsOrder::getStatus,
						OrderStatusEnum.PENDING.getCode(),
						OrderStatusEnum.CONFIRMED.getCode(),
						OrderStatusEnum.TRADING.getCode()));
		if (activeCount > 0) {
			throw new BusinessException(400, "该商品已有进行中的订单");
		}

		GoodsOrder order = new GoodsOrder();
		order.setOrderNo(generateOrderNo());
		order.setGoodsId(goods.getId());
		order.setBuyerId(buyerId);
		order.setSellerId(goods.getUserId());
		order.setDealPrice(goods.getPrice());
		order.setStatus(OrderStatusEnum.PENDING.getCode());
		order.setRemark(dto.getRemark());
		order.setCreateTime(LocalDateTime.now());
		order.setUpdateTime(LocalDateTime.now());
		save(order);

		goods.setStatus(GoodsStatusEnum.OFF_SHELF.getCode());
		goods.setUpdateTime(LocalDateTime.now());
		goodsInfoService.updateById(goods);

		saveOrderLog(order.getId(), "create", buyerId, "买家创建订单");
		return order.getId();
	}

	@Override
	public PageResult<OrderDetailVO> buyerOrders(String token, PageQuery query) {
		Long buyerId = getUserIdByToken(token);
		return pageOrders(query, new LambdaQueryWrapper<GoodsOrder>()
				.eq(GoodsOrder::getBuyerId, buyerId)
				.orderByDesc(GoodsOrder::getCreateTime));
	}

	@Override
	public PageResult<OrderDetailVO> sellerOrders(String token, PageQuery query) {
		Long sellerId = getUserIdByToken(token);
		return pageOrders(query, new LambdaQueryWrapper<GoodsOrder>()
				.eq(GoodsOrder::getSellerId, sellerId)
				.orderByDesc(GoodsOrder::getCreateTime));
	}

	@Override
	public OrderDetailVO orderDetail(String token, Long id) {
		Long userId = getUserIdByToken(token);
		GoodsOrder order = getById(id);
		if (order == null) {
			throw new BusinessException(404, "订单不存在");
		}
		requireOrderParticipant(order, userId);
		return toVO(order, loadGoodsMap(List.of(order.getGoodsId())), loadUserMap(List.of(order.getBuyerId(), order.getSellerId())));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelOrder(String token, Long id) {
		Long userId = getUserIdByToken(token);
		GoodsOrder order = getById(id);
		if (order == null) {
			throw new BusinessException(404, "订单不存在");
		}
		if (!Objects.equals(order.getBuyerId(), userId)) {
			throw new BusinessException(403, "仅买家可取消订单");
		}
		if (!OrderStatusEnum.PENDING.getCode().equals(order.getStatus())
				&& !OrderStatusEnum.CONFIRMED.getCode().equals(order.getStatus())) {
			throw new BusinessException(400, "当前状态不可取消");
		}

		order.setStatus(OrderStatusEnum.CANCELLED.getCode());
		order.setUpdateTime(LocalDateTime.now());
		updateById(order);

		GoodsInfo goods = goodsInfoService.getById(order.getGoodsId());
		if (goods != null && !GoodsStatusEnum.SOLD.getCode().equals(goods.getStatus())) {
			goods.setStatus(GoodsStatusEnum.ON_SALE.getCode());
			goods.setUpdateTime(LocalDateTime.now());
			goodsInfoService.updateById(goods);
		}

		saveOrderLog(order.getId(), "cancel", userId, "买家取消订单");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void confirmOrder(String token, Long id) {
		Long userId = getUserIdByToken(token);
		GoodsOrder order = getById(id);
		if (order == null) {
			throw new BusinessException(404, "订单不存在");
		}
		if (!Objects.equals(order.getSellerId(), userId)) {
			throw new BusinessException(403, "仅卖家可确认订单");
		}
		if (!OrderStatusEnum.PENDING.getCode().equals(order.getStatus())) {
			throw new BusinessException(400, "当前状态不可确认");
		}

		order.setStatus(OrderStatusEnum.CONFIRMED.getCode());
		order.setUpdateTime(LocalDateTime.now());
		updateById(order);
		saveOrderLog(order.getId(), "confirm", userId, "卖家确认订单");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void completeOrder(String token, Long id) {
		Long userId = getUserIdByToken(token);
		GoodsOrder order = getById(id);
		if (order == null) {
			throw new BusinessException(404, "订单不存在");
		}
		if (!Objects.equals(order.getBuyerId(), userId)) {
			throw new BusinessException(403, "仅买家可完成订单");
		}
		if (!OrderStatusEnum.CONFIRMED.getCode().equals(order.getStatus())
				&& !OrderStatusEnum.TRADING.getCode().equals(order.getStatus())) {
			throw new BusinessException(400, "当前状态不可完成");
		}

		order.setStatus(OrderStatusEnum.COMPLETED.getCode());
		order.setUpdateTime(LocalDateTime.now());
		order.setFinishTime(LocalDateTime.now());
		updateById(order);

		GoodsInfo goods = goodsInfoService.getById(order.getGoodsId());
		if (goods != null) {
			goods.setStatus(GoodsStatusEnum.SOLD.getCode());
			goods.setUpdateTime(LocalDateTime.now());
			goodsInfoService.updateById(goods);
		}
		saveOrderLog(order.getId(), "complete", userId, "买家完成订单");
	}

	private PageResult<OrderDetailVO> pageOrders(PageQuery query, LambdaQueryWrapper<GoodsOrder> wrapper) {
		Page<GoodsOrder> page = query.toPage();
		Page<GoodsOrder> result = this.page(page, wrapper);
		if (result.getRecords() == null || result.getRecords().isEmpty()) {
			return PageResult.empty(query.getPageNum(), query.getPageSize());
		}

		List<Long> goodsIds = result.getRecords().stream().map(GoodsOrder::getGoodsId).distinct().collect(Collectors.toList());
		List<Long> userIds = result.getRecords().stream()
				.flatMap(o -> java.util.stream.Stream.of(o.getBuyerId(), o.getSellerId()))
				.distinct()
				.collect(Collectors.toList());
		Map<Long, GoodsInfo> goodsMap = loadGoodsMap(goodsIds);
		Map<Long, SysUser> userMap = loadUserMap(userIds);

		List<OrderDetailVO> records = result.getRecords().stream()
				.map(order -> toVO(order, goodsMap, userMap))
				.collect(Collectors.toList());
		return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
	}

	private OrderDetailVO toVO(GoodsOrder order, Map<Long, GoodsInfo> goodsMap, Map<Long, SysUser> userMap) {
		OrderDetailVO vo = new OrderDetailVO();
		vo.setId(order.getId());
		vo.setOrderNo(order.getOrderNo());
		vo.setGoodsId(order.getGoodsId());
		GoodsInfo goods = goodsMap.get(order.getGoodsId());
		vo.setGoodsTitle(goods == null ? null : goods.getTitle());
		vo.setGoodsMainImage(goods == null ? null : goods.getMainImage());

		vo.setBuyerId(order.getBuyerId());
		SysUser buyer = userMap.get(order.getBuyerId());
		vo.setBuyerName(buyer == null ? null : (StringUtils.isNotBlank(buyer.getNickname()) ? buyer.getNickname() : buyer.getUsername()));

		vo.setSellerId(order.getSellerId());
		SysUser seller = userMap.get(order.getSellerId());
		vo.setSellerName(seller == null ? null : (StringUtils.isNotBlank(seller.getNickname()) ? seller.getNickname() : seller.getUsername()));

		vo.setDealPrice(order.getDealPrice());
		vo.setStatus(order.getStatus());
		vo.setRemark(order.getRemark());
		vo.setCreateTime(order.getCreateTime());
		vo.setUpdateTime(order.getUpdateTime());
		vo.setFinishTime(order.getFinishTime());
		return vo;
	}

	private Map<Long, GoodsInfo> loadGoodsMap(List<Long> goodsIds) {
		if (goodsIds == null || goodsIds.isEmpty()) {
			return Map.of();
		}
		List<GoodsInfo> goodsList = goodsInfoService.listByIds(goodsIds);
		Map<Long, GoodsInfo> map = new HashMap<>();
		for (GoodsInfo goods : goodsList) {
			map.put(goods.getId(), goods);
		}
		return map;
	}

	private Map<Long, SysUser> loadUserMap(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Map.of();
		}
		List<SysUser> users = sysUserService.listByIds(userIds);
		Map<Long, SysUser> map = new HashMap<>();
		for (SysUser user : users) {
			map.put(user.getId(), user);
		}
		return map;
	}

	private void requireOrderParticipant(GoodsOrder order, Long userId) {
		if (!Objects.equals(order.getBuyerId(), userId) && !Objects.equals(order.getSellerId(), userId)) {
			throw new BusinessException(403, "无权限查看该订单");
		}
	}

	private void saveOrderLog(Long orderId, String action, Long operatorId, String content) {
		GoodsOrderLog log = new GoodsOrderLog();
		log.setOrderId(orderId);
		log.setAction(action);
		log.setOperatorId(operatorId);
		log.setContent(content);
		log.setCreateTime(LocalDateTime.now());
		goodsOrderLogService.save(log);
	}

	private String generateOrderNo() {
		return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
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
