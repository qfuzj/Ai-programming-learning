package com.campus.resourcesharing.controller.goods;

import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.goods.GoodsAddDTO;
import com.campus.resourcesharing.dto.goods.GoodsUpdateDTO;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.query.GoodsPageQuery;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.vo.goods.GoodsDetailVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsInfoService goodsInfoService;

    public GoodsController(GoodsInfoService goodsInfoService) {
        this.goodsInfoService = goodsInfoService;
    }

    @PostMapping("/add")
    public Result<Map<String, Long>> add(@RequestHeader("Authorization") String authorization,
                                         @Valid @RequestBody GoodsAddDTO dto) {
        Long id = goodsInfoService.addGoods(extractToken(authorization), dto);
        return Result.success("发布成功", Map.of("id", id));
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestHeader("Authorization") String authorization,
                               @Valid @RequestBody GoodsUpdateDTO dto) {
        goodsInfoService.updateGoods(extractToken(authorization), dto);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id) {
        goodsInfoService.deleteGoods(extractToken(authorization), id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/detail/{id}")
    public Result<GoodsDetailVO> detail(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractTokenNullable(authorization);
        return Result.success(goodsInfoService.getDetail(token, id));
    }

    @GetMapping("/my/list")
    public Result<PageResult<GoodsInfo>> myList(@RequestHeader("Authorization") String authorization,
                                                @Valid GoodsPageQuery query) {
        return Result.success(goodsInfoService.myGoods(extractToken(authorization), query));
    }

    @PutMapping("/putaway/{id}")
    public Result<Void> putaway(@RequestHeader("Authorization") String authorization,
                                @PathVariable Long id) {
        goodsInfoService.putaway(extractToken(authorization), id);
        return Result.success("上架成功", null);
    }

    @PutMapping("/soldout/{id}")
    public Result<Void> soldout(@RequestHeader("Authorization") String authorization,
                                @PathVariable Long id) {
        goodsInfoService.soldout(extractToken(authorization), id);
        return Result.success("下架成功", null);
    }

    private String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }

    private String extractTokenNullable(String authorization) {
        return extractToken(authorization);
    }
}
