package com.campus.resourcesharing.service;

import com.campus.resourcesharing.vo.home.BannerVO;
import com.campus.resourcesharing.vo.home.NoticeVO;
import com.campus.resourcesharing.vo.home.GoodsHomeVO;
import java.util.List;

public interface HomeService {
    
    /**
     * 获取轮播图列表
     */
    List<BannerVO> getBanners();
    
    /**
     * 获取推荐商品列表
     */
    List<GoodsHomeVO> getRecommendGoods();
    
    /**
     * 获取最新商品列表
     */
    List<GoodsHomeVO> getLatestGoods();
    
    /**
     * 获取公告列表
     */
    List<NoticeVO> getNotices();
}
