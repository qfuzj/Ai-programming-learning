package com.travel.advisor.service;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.vo.favorite.FavoriteVO;

public interface FavoriteService {

    void addFavorite(Long scenicId);

    void removeFavorite(Long scenicId);

    PageResult<FavoriteVO> pageFavorites(PageQuery pageQuery);
}
