package com.campus.resourcesharing.controller.common;

import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.query.GoodsPageQuery;
import com.campus.resourcesharing.service.GoodsInfoService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/goods")
public class GoodsQueryController {

    private final GoodsInfoService goodsInfoService;

    public GoodsQueryController(GoodsInfoService goodsInfoService) {
        this.goodsInfoService = goodsInfoService;
    }

    @GetMapping("/list")
    public Result<PageResult<GoodsInfo>> pageGoods(@Valid GoodsPageQuery query) {
        return Result.success(goodsInfoService.pageGoods(query));
    }
}
