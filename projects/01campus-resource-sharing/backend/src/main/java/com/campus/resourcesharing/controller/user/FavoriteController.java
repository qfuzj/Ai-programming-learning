package com.campus.resourcesharing.controller.user;

import com.campus.resourcesharing.common.query.PageQuery;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.favorite.FavoriteAddDTO;
import com.campus.resourcesharing.service.GoodsFavoriteService;
import com.campus.resourcesharing.vo.favorite.FavoriteGoodsVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final GoodsFavoriteService goodsFavoriteService;

    public FavoriteController(GoodsFavoriteService goodsFavoriteService) {
        this.goodsFavoriteService = goodsFavoriteService;
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestHeader("Authorization") String authorization,
                            @Valid @RequestBody FavoriteAddDTO dto) {
        goodsFavoriteService.addFavorite(extractToken(authorization), dto.getGoodsId());
        return Result.success("收藏成功", null);
    }

    @DeleteMapping("/remove/{goodsId}")
    public Result<Void> remove(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long goodsId) {
        goodsFavoriteService.removeFavorite(extractToken(authorization), goodsId);
        return Result.success("取消收藏成功", null);
    }

    @GetMapping("/list")
    public Result<PageResult<FavoriteGoodsVO>> list(@RequestHeader("Authorization") String authorization,
                                                     @Valid PageQuery query) {
        return Result.success(goodsFavoriteService.listMyFavorites(extractToken(authorization), query));
    }

    @GetMapping("/status/{goodsId}")
    public Result<Boolean> status(@RequestHeader("Authorization") String authorization,
                                  @PathVariable Long goodsId) {
        return Result.success(goodsFavoriteService.isFavorited(extractToken(authorization), goodsId));
    }

    private String extractToken(String authorization) {
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }
}
