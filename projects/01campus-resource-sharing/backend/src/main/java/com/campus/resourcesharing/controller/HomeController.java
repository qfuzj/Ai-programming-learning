package com.campus.resourcesharing.controller;

import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.service.HomeService;
import com.campus.resourcesharing.vo.home.BannerVO;
import com.campus.resourcesharing.vo.home.GoodsHomeVO;
import com.campus.resourcesharing.vo.home.NoticeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/banners")
    public Result<List<BannerVO>> getBanners() {
        List<BannerVO> banners = homeService.getBanners();
        return Result.success("获取轮播图成功", banners);
    }

    @GetMapping("/recommend")
    public Result<List<GoodsHomeVO>> getRecommend() {
        List<GoodsHomeVO> goods = homeService.getRecommendGoods();
        return Result.success("获取推荐商品成功", goods);
    }

    @GetMapping("/latest")
    public Result<List<GoodsHomeVO>> getLatest() {
        List<GoodsHomeVO> goods = homeService.getLatestGoods();
        return Result.success("获取最新商品成功", goods);
    }

    @GetMapping("/notices")
    public Result<List<NoticeVO>> getNotices() {
        List<NoticeVO> notices = homeService.getNotices();
        return Result.success("获取公告成功", notices);
    }
}
