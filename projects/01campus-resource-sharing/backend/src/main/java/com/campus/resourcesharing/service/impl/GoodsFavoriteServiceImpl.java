package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.common.query.PageQuery;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.entity.GoodsFavorite;
import com.campus.resourcesharing.mapper.GoodsFavoriteMapper;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.service.GoodsFavoriteService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.favorite.FavoriteGoodsVO;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GoodsFavoriteServiceImpl extends ServiceImpl<GoodsFavoriteMapper, GoodsFavorite> implements GoodsFavoriteService {

	private final GoodsInfoService goodsInfoService;
	private final JwtUtil jwtUtil;

	public GoodsFavoriteServiceImpl(GoodsInfoService goodsInfoService, JwtUtil jwtUtil) {
		this.goodsInfoService = goodsInfoService;
		this.jwtUtil = jwtUtil;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addFavorite(String token, Long goodsId) {
		Long userId = getUserIdByToken(token);
		GoodsInfo goods = goodsInfoService.getById(goodsId);
		if (goods == null || (goods.getDeleted() != null && goods.getDeleted() == 1)) {
			throw new BusinessException(404, "商品不存在");
		}
		if (Objects.equals(goods.getUserId(), userId)) {
			throw new BusinessException(400, "不能收藏自己的商品");
		}

		GoodsFavorite existed = getOne(new LambdaQueryWrapper<GoodsFavorite>()
				.eq(GoodsFavorite::getUserId, userId)
				.eq(GoodsFavorite::getGoodsId, goodsId));
		if (existed != null) {
			throw new BusinessException(400, "请勿重复收藏");
		}

		GoodsFavorite favorite = new GoodsFavorite();
		favorite.setUserId(userId);
		favorite.setGoodsId(goodsId);
		save(favorite);

		int count = goods.getFavoriteCount() == null ? 0 : goods.getFavoriteCount();
		goods.setFavoriteCount(count + 1);
		goodsInfoService.updateById(goods);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeFavorite(String token, Long goodsId) {
		Long userId = getUserIdByToken(token);
		GoodsFavorite existed = getOne(new LambdaQueryWrapper<GoodsFavorite>()
				.eq(GoodsFavorite::getUserId, userId)
				.eq(GoodsFavorite::getGoodsId, goodsId));
		if (existed == null) {
			throw new BusinessException(404, "收藏记录不存在");
		}
		removeById(existed.getId());

		GoodsInfo goods = goodsInfoService.getById(goodsId);
		if (goods != null) {
			int count = goods.getFavoriteCount() == null ? 0 : goods.getFavoriteCount();
			goods.setFavoriteCount(Math.max(count - 1, 0));
			goodsInfoService.updateById(goods);
		}
	}

	@Override
	public PageResult<FavoriteGoodsVO> listMyFavorites(String token, PageQuery query) {
		Long userId = getUserIdByToken(token);

		Page<GoodsFavorite> page = query.toPage();
		LambdaQueryWrapper<GoodsFavorite> wrapper = new LambdaQueryWrapper<GoodsFavorite>()
				.eq(GoodsFavorite::getUserId, userId)
				.orderByDesc(GoodsFavorite::getCreateTime);
		Page<GoodsFavorite> favoritePage = this.page(page, wrapper);
		if (favoritePage.getRecords() == null || favoritePage.getRecords().isEmpty()) {
			return PageResult.empty(query.getPageNum(), query.getPageSize());
		}

		List<Long> goodsIds = favoritePage.getRecords().stream()
				.map(GoodsFavorite::getGoodsId)
				.collect(Collectors.toList());
		List<GoodsInfo> goodsList = goodsInfoService.list(new LambdaQueryWrapper<GoodsInfo>()
				.in(GoodsInfo::getId, goodsIds));
		Map<Long, GoodsInfo> goodsMap = new HashMap<>();
		for (GoodsInfo goods : goodsList) {
			goodsMap.put(goods.getId(), goods);
		}

		List<FavoriteGoodsVO> records = favoritePage.getRecords().stream()
				.map(favorite -> {
					GoodsInfo goods = goodsMap.get(favorite.getGoodsId());
					if (goods == null || (goods.getDeleted() != null && goods.getDeleted() == 1)) {
						return null;
					}
					FavoriteGoodsVO vo = new FavoriteGoodsVO();
					vo.setFavoriteId(favorite.getId());
					vo.setFavoriteTime(favorite.getCreateTime());
					vo.setGoodsId(goods.getId());
					vo.setUserId(goods.getUserId());
					vo.setCategoryId(goods.getCategoryId());
					vo.setTitle(goods.getTitle());
					vo.setPrice(goods.getPrice());
					vo.setConditionLevel(goods.getConditionLevel());
					vo.setMainImage(goods.getMainImage());
					vo.setStatus(goods.getStatus());
					vo.setFavoriteCount(goods.getFavoriteCount());
					vo.setViewCount(goods.getViewCount());
					vo.setGoodsCreateTime(goods.getCreateTime());
					return vo;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		return new PageResult<>(favoritePage.getTotal(), favoritePage.getCurrent(), favoritePage.getSize(), records);
	}

	@Override
	public boolean isFavorited(String token, Long goodsId) {
		Long userId = getUserIdNullable(token);
		if (userId == null) {
			return false;
		}
		return count(new LambdaQueryWrapper<GoodsFavorite>()
				.eq(GoodsFavorite::getUserId, userId)
				.eq(GoodsFavorite::getGoodsId, goodsId)) > 0;
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
