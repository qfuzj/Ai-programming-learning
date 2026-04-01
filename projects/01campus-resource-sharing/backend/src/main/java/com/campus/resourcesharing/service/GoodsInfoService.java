package com.campus.resourcesharing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.dto.goods.GoodsAddDTO;
import com.campus.resourcesharing.dto.goods.GoodsUpdateDTO;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.query.GoodsPageQuery;
import com.campus.resourcesharing.vo.goods.GoodsDetailVO;

public interface GoodsInfoService extends IService<GoodsInfo> {

    PageResult<GoodsInfo> pageGoods(GoodsPageQuery query);

    Long addGoods(String token, GoodsAddDTO dto);

    void updateGoods(String token, GoodsUpdateDTO dto);

    void deleteGoods(String token, Long id);

    GoodsDetailVO getDetail(String token, Long id);

    PageResult<GoodsInfo> myGoods(String token, GoodsPageQuery query);

    void putaway(String token, Long id);

    void soldout(String token, Long id);
}
