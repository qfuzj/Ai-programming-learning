package com.travel.advisor.controller.user;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.service.FavoriteService;
import com.travel.advisor.vo.favorite.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{scenicId}")
    public Result<Void> add(@PathVariable Long scenicId) {
        favoriteService.addFavorite(scenicId);
        return Result.success();
    }

    @DeleteMapping("/{scenicId}")
    public Result<Void> remove(@PathVariable Long scenicId) {
        favoriteService.removeFavorite(scenicId);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<FavoriteVO>> page(PageQuery pageQuery) {
        return Result.success(favoriteService.pageFavorites(pageQuery));
    }
}
