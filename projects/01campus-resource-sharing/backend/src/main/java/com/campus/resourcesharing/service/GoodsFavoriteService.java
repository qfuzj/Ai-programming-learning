package com.campus.resourcesharing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.resourcesharing.common.query.PageQuery;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.entity.GoodsFavorite;
import com.campus.resourcesharing.vo.favorite.FavoriteGoodsVO;

public interface GoodsFavoriteService extends IService<GoodsFavorite> {

	void addFavorite(String token, Long goodsId);

	void removeFavorite(String token, Long goodsId);

	PageResult<FavoriteGoodsVO> listMyFavorites(String token, PageQuery query);

	boolean isFavorited(String token, Long goodsId);
}
