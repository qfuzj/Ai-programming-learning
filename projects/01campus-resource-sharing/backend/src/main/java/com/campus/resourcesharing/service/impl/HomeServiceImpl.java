package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.entity.GoodsImage;
import com.campus.resourcesharing.entity.SysBanner;
import com.campus.resourcesharing.entity.SysNotice;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.service.GoodsImageService;
import com.campus.resourcesharing.service.HomeService;
import com.campus.resourcesharing.service.SysBannerService;
import com.campus.resourcesharing.service.SysNoticeService;
import com.campus.resourcesharing.vo.home.BannerVO;
import com.campus.resourcesharing.vo.home.GoodsHomeVO;
import com.campus.resourcesharing.vo.home.NoticeVO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HomeServiceImpl implements HomeService {

    private final SysBannerService sysBannerService;
    private final GoodsInfoService goodsInfoService;
    private final GoodsImageService goodsImageService;
    private final SysNoticeService sysNoticeService;

    public HomeServiceImpl(SysBannerService sysBannerService, 
                         GoodsInfoService goodsInfoService,
                         GoodsImageService goodsImageService,
                         SysNoticeService sysNoticeService) {
        this.sysBannerService = sysBannerService;
        this.goodsInfoService = goodsInfoService;
        this.goodsImageService = goodsImageService;
        this.sysNoticeService = sysNoticeService;
    }

    @Override
    public List<BannerVO> getBanners() {
        List<SysBanner> banners = sysBannerService.list(
            new LambdaQueryWrapper<SysBanner>()
                .eq(SysBanner::getStatus, 1)
                .orderByAsc(SysBanner::getSort)
        );
        
        return banners.stream()
                .map(banner -> new BannerVO(
                    banner.getId(),
                    banner.getTitle(),
                    banner.getImageUrl(),
                    banner.getLinkUrl(),
                    banner.getSort()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<GoodsHomeVO> getRecommendGoods() {
        // 推荐逻辑：获取收藏数最多的商品，取前8条
        List<GoodsInfo> goods = goodsInfoService.list(
            new LambdaQueryWrapper<GoodsInfo>()
                .eq(GoodsInfo::getStatus, "on_sale")
                .eq(GoodsInfo::getDeleted, 0)
                .orderByDesc(GoodsInfo::getFavoriteCount)
                .last("LIMIT 8")
        );
        
        return goods.stream()
                .map(this::toGoodsHomeVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoodsHomeVO> getLatestGoods() {
        // 最新逻辑：按创建时间倒序，取前8条
        List<GoodsInfo> goods = goodsInfoService.list(
            new LambdaQueryWrapper<GoodsInfo>()
                .eq(GoodsInfo::getStatus, "on_sale")
                .eq(GoodsInfo::getDeleted, 0)
                .orderByDesc(GoodsInfo::getCreateTime)
                .last("LIMIT 8")
        );
        
        return goods.stream()
                .map(this::toGoodsHomeVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoticeVO> getNotices() {
        List<SysNotice> notices = sysNoticeService.list(
            new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, 1)
                .orderByDesc(SysNotice::getCreateTime)
                .last("LIMIT 10")
        );
        
        return notices.stream()
                .map(notice -> new NoticeVO(
                    notice.getId(),
                    notice.getTitle(),
                    notice.getContent(),
                    notice.getCreateTime()
                ))
                .collect(Collectors.toList());
    }

    private GoodsHomeVO toGoodsHomeVO(GoodsInfo goods) {
        return new GoodsHomeVO(
            goods.getId(),
            goods.getTitle(),
            goods.getCategoryId(),
            goods.getPrice(),
            resolveMainImage(goods),
            goods.getConditionLevel(),
            goods.getFavoriteCount(),
            goods.getViewCount(),
            goods.getStatus(),
            goods.getCreateTime()
        );
    }

    private String resolveMainImage(GoodsInfo goods) {
        if (goods == null) {
            return null;
        }
        if (StringUtils.isNotBlank(goods.getMainImage())) {
            return goods.getMainImage();
        }

        List<GoodsImage> images = goodsImageService.list(
                new LambdaQueryWrapper<GoodsImage>()
                        .eq(GoodsImage::getGoodsId, goods.getId())
                        .orderByAsc(GoodsImage::getSort)
                        .last("LIMIT 1")
        );
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0).getImageUrl();
    }
}
