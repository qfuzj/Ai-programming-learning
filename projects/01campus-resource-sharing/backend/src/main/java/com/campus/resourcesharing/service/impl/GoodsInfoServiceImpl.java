package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.common.enums.GoodsStatusEnum;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.dto.goods.GoodsAddDTO;
import com.campus.resourcesharing.dto.goods.GoodsUpdateDTO;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.entity.GoodsFavorite;
import com.campus.resourcesharing.entity.GoodsImage;
import com.campus.resourcesharing.mapper.GoodsFavoriteMapper;
import com.campus.resourcesharing.mapper.GoodsInfoMapper;
import com.campus.resourcesharing.query.GoodsPageQuery;
import com.campus.resourcesharing.service.GoodsImageService;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.goods.GoodsDetailVO;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GoodsInfoServiceImpl extends ServiceImpl<GoodsInfoMapper, GoodsInfo> implements GoodsInfoService {

    private final GoodsInfoMapper goodsInfoMapper;
    private final GoodsImageService goodsImageService;
    private final GoodsFavoriteMapper goodsFavoriteMapper;
    private final JwtUtil jwtUtil;

    public GoodsInfoServiceImpl(GoodsInfoMapper goodsInfoMapper,
                                GoodsImageService goodsImageService,
                                GoodsFavoriteMapper goodsFavoriteMapper,
                                JwtUtil jwtUtil) {
        this.goodsInfoMapper = goodsInfoMapper;
        this.goodsImageService = goodsImageService;
        this.goodsFavoriteMapper = goodsFavoriteMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public PageResult<GoodsInfo> pageGoods(GoodsPageQuery query) {
        Page<GoodsInfo> page = query.toPage();
        LambdaQueryWrapper<GoodsInfo> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.isNotBlank(query.getKeyword()), GoodsInfo::getTitle, query.getKeyword())
                .eq(Objects.nonNull(query.getCategoryId()), GoodsInfo::getCategoryId, query.getCategoryId())
                .eq(StringUtils.isNotBlank(query.getConditionLevel()), GoodsInfo::getConditionLevel, query.getConditionLevel())
                .ge(Objects.nonNull(query.getMinPrice()), GoodsInfo::getPrice, query.getMinPrice())
                .le(Objects.nonNull(query.getMaxPrice()), GoodsInfo::getPrice, query.getMaxPrice())
                .eq(GoodsInfo::getStatus, GoodsStatusEnum.ON_SALE.getCode());

        String sortType = query.getSortType();
        if ("priceAsc".equals(sortType)) {
            wrapper.orderByAsc(GoodsInfo::getPrice);
        } else if ("priceDesc".equals(sortType)) {
            wrapper.orderByDesc(GoodsInfo::getPrice);
        } else if ("hot".equals(sortType)) {
            wrapper.orderByDesc(GoodsInfo::getFavoriteCount);
        } else {
            wrapper.orderByDesc(GoodsInfo::getCreateTime);
        }

        Page<GoodsInfo> result = goodsInfoMapper.selectPage(page, wrapper);
        return PageResult.from(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addGoods(String token, GoodsAddDTO dto) {
        Long userId = getUserIdByToken(token);

        GoodsInfo goods = new GoodsInfo();
        goods.setUserId(userId);
        goods.setCategoryId(dto.getCategoryId());
        goods.setTitle(dto.getTitle());
        goods.setDescription(dto.getDescription());
        goods.setPrice(dto.getPrice());
        goods.setOriginalPrice(dto.getOriginalPrice());
        goods.setConditionLevel(dto.getConditionLevel());
        goods.setContactInfo(dto.getContactInfo());
        goods.setTradeLocation(dto.getTradeLocation());
        goods.setMainImage(dto.getMainImage());
        goods.setViewCount(0);
        goods.setFavoriteCount(0);
        goods.setStatus(GoodsStatusEnum.ON_SALE.getCode());
        goods.setDeleted(0);
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());

        save(goods);
        saveImages(goods.getId(), dto.getImageList());
        return goods.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(String token, GoodsUpdateDTO dto) {
        GoodsInfo goods = requireOwnedGoods(token, dto.getId());

        if (GoodsStatusEnum.SOLD.getCode().equals(goods.getStatus())) {
            throw new BusinessException(400, "已售出商品不可编辑");
        }

        goods.setCategoryId(dto.getCategoryId());
        goods.setTitle(dto.getTitle());
        goods.setDescription(dto.getDescription());
        goods.setPrice(dto.getPrice());
        goods.setOriginalPrice(dto.getOriginalPrice());
        goods.setConditionLevel(dto.getConditionLevel());
        goods.setContactInfo(dto.getContactInfo());
        goods.setTradeLocation(dto.getTradeLocation());
        goods.setMainImage(dto.getMainImage());
        goods.setUpdateTime(LocalDateTime.now());
        updateById(goods);

        goodsImageService.remove(new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, goods.getId()));
        saveImages(goods.getId(), dto.getImageList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(String token, Long id) {
        GoodsInfo goods = requireOwnedGoods(token, id);
        removeById(goods.getId());
        goodsImageService.remove(new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, goods.getId()));
    }

    @Override
    public GoodsDetailVO getDetail(String token, Long id) {
        GoodsInfo goods = getById(id);
        if (goods == null || goods.getDeleted() != null && goods.getDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }

        GoodsDetailVO vo = new GoodsDetailVO();
        vo.setId(goods.getId());
        vo.setUserId(goods.getUserId());
        vo.setCategoryId(goods.getCategoryId());
        vo.setTitle(goods.getTitle());
        vo.setDescription(goods.getDescription());
        vo.setPrice(goods.getPrice());
        vo.setOriginalPrice(goods.getOriginalPrice());
        vo.setConditionLevel(goods.getConditionLevel());
        vo.setContactInfo(goods.getContactInfo());
        vo.setTradeLocation(goods.getTradeLocation());
        vo.setMainImage(goods.getMainImage());
        vo.setViewCount(goods.getViewCount());
        vo.setFavoriteCount(goods.getFavoriteCount());
        vo.setStatus(goods.getStatus());
        vo.setCreateTime(goods.getCreateTime());
        vo.setImageList(listImageUrls(goods.getId()));

        Long currentUserId = getUserIdNullable(token);
        vo.setOwner(currentUserId != null && Objects.equals(currentUserId, goods.getUserId()));
        boolean favorited = currentUserId != null && goodsFavoriteMapper.selectCount(
            new LambdaQueryWrapper<GoodsFavorite>()
                .eq(GoodsFavorite::getUserId, currentUserId)
                .eq(GoodsFavorite::getGoodsId, goods.getId())
        ) > 0;
        vo.setFavorited(favorited);
        return vo;
    }

    @Override
    public PageResult<GoodsInfo> myGoods(String token, GoodsPageQuery query) {
        Long userId = getUserIdByToken(token);
        Page<GoodsInfo> page = query.toPage();
        LambdaQueryWrapper<GoodsInfo> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(GoodsInfo::getUserId, userId)
                .like(StringUtils.isNotBlank(query.getKeyword()), GoodsInfo::getTitle, query.getKeyword())
                .eq(Objects.nonNull(query.getCategoryId()), GoodsInfo::getCategoryId, query.getCategoryId())
                .eq(StringUtils.isNotBlank(query.getConditionLevel()), GoodsInfo::getConditionLevel, query.getConditionLevel())
                .orderByDesc(GoodsInfo::getCreateTime);

        Page<GoodsInfo> result = goodsInfoMapper.selectPage(page, wrapper);
        return PageResult.from(result);
    }

    @Override
    public void putaway(String token, Long id) {
        GoodsInfo goods = requireOwnedGoods(token, id);
        if (GoodsStatusEnum.SOLD.getCode().equals(goods.getStatus())) {
            throw new BusinessException(400, "已售出商品不可上架");
        }
        goods.setStatus(GoodsStatusEnum.ON_SALE.getCode());
        goods.setUpdateTime(LocalDateTime.now());
        updateById(goods);
    }

    @Override
    public void soldout(String token, Long id) {
        GoodsInfo goods = requireOwnedGoods(token, id);
        goods.setStatus(GoodsStatusEnum.OFF_SHELF.getCode());
        goods.setUpdateTime(LocalDateTime.now());
        updateById(goods);
    }

    private void saveImages(Long goodsId, List<String> imageList) {
        if (imageList == null || imageList.isEmpty()) {
            return;
        }
        int sort = 1;
        for (String imageUrl : imageList) {
            if (StringUtils.isBlank(imageUrl)) {
                continue;
            }
            GoodsImage image = new GoodsImage();
            image.setGoodsId(goodsId);
            image.setImageUrl(imageUrl);
            image.setSort(sort++);
            image.setCreateTime(LocalDateTime.now());
            goodsImageService.save(image);
        }
    }

    private List<String> listImageUrls(Long goodsId) {
        List<GoodsImage> images = goodsImageService.list(
                new LambdaQueryWrapper<GoodsImage>()
                        .eq(GoodsImage::getGoodsId, goodsId)
                        .orderByAsc(GoodsImage::getSort)
        );
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return images.stream().map(GoodsImage::getImageUrl).collect(Collectors.toList());
    }

    private GoodsInfo requireOwnedGoods(String token, Long goodsId) {
        Long userId = getUserIdByToken(token);
        GoodsInfo goods = getById(goodsId);
        if (goods == null || goods.getDeleted() != null && goods.getDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!Objects.equals(goods.getUserId(), userId)) {
            throw new BusinessException(403, "无权限操作他人商品");
        }
        return goods;
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
