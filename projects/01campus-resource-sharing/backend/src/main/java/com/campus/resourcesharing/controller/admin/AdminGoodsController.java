package com.campus.resourcesharing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.admin.AdminGoodsStatusDTO;
import com.campus.resourcesharing.entity.GoodsInfo;
import com.campus.resourcesharing.query.admin.AdminGoodsPageQuery;
import com.campus.resourcesharing.service.admin.AdminUserLookupService;
import com.campus.resourcesharing.service.GoodsInfoService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.admin.AdminGoodsVO;
import com.campus.resourcesharing.vo.admin.AdminGoodsDetailVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/goods")
public class AdminGoodsController extends AdminBaseController {

    private final GoodsInfoService goodsInfoService;
    private final AdminUserLookupService adminUserLookupService;

    public AdminGoodsController(JwtUtil jwtUtil,
                                GoodsInfoService goodsInfoService,
                                AdminUserLookupService adminUserLookupService) {
        super(jwtUtil);
        this.goodsInfoService = goodsInfoService;
        this.adminUserLookupService = adminUserLookupService;
    }

    @GetMapping("/page")
    public Result<PageResult<AdminGoodsVO>> page(@RequestHeader("Authorization") String authorization,
                                                 @Valid AdminGoodsPageQuery query) {
        assertAdmin(authorization);

        String keyword = query.getKeyword() == null ? null : query.getKeyword().trim();
        boolean hasKeyword = keyword != null && !keyword.isEmpty();

        boolean hasUserKeyword = query.getUserKeyword() != null && !query.getUserKeyword().isBlank();
        Set<Long> userIds = adminUserLookupService.findUserIdsByKeyword(query.getUserKeyword());
        if (hasUserKeyword && userIds.isEmpty()) {
            return Result.success(PageResult.empty(query.getPageNum(), query.getPageSize()));
        }

        LambdaQueryWrapper<GoodsInfo> wrapper = new LambdaQueryWrapper<GoodsInfo>()
                .eq(GoodsInfo::getDeleted, 0)
                .eq(query.getCategoryId() != null, GoodsInfo::getCategoryId, query.getCategoryId())
                .eq(query.getUserId() != null, GoodsInfo::getUserId, query.getUserId())
                .in(hasUserKeyword, GoodsInfo::getUserId, userIds)
                .eq(query.getStatus() != null && !query.getStatus().isBlank(), GoodsInfo::getStatus, query.getStatus())
                .orderByDesc(GoodsInfo::getCreateTime);

        if (hasKeyword) {
            wrapper.and(w -> w.like(GoodsInfo::getTitle, keyword).or().like(GoodsInfo::getDescription, keyword));
        }

        Page<GoodsInfo> page = goodsInfoService.page(query.toPage(), wrapper);
        Map<Long, String> userNameMap = adminUserLookupService.buildUserDisplayNameMap(
                page.getRecords().stream().map(GoodsInfo::getUserId).collect(Collectors.toSet())
        );

        List<AdminGoodsVO> records = page.getRecords().stream().map(item -> {
            AdminGoodsVO vo = new AdminGoodsVO();
            vo.setId(item.getId());
            vo.setUserId(item.getUserId());
            vo.setUserName(userNameMap.get(item.getUserId()));
            vo.setCategoryId(item.getCategoryId());
            vo.setTitle(item.getTitle());
            vo.setPrice(item.getPrice());
            vo.setStatus(item.getStatus());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).toList();

        return Result.success(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), records));
    }

    @GetMapping("/detail/{id}")
    public Result<AdminGoodsDetailVO> detail(@RequestHeader("Authorization") String authorization,
                                             @PathVariable Long id) {
        assertAdmin(authorization);

        GoodsInfo goods = goodsInfoService.getById(id);
        if (goods == null || (goods.getDeleted() != null && goods.getDeleted() == 1)) {
            throw new BusinessException(404, "商品不存在");
        }

        AdminGoodsDetailVO vo = new AdminGoodsDetailVO();
        vo.setId(goods.getId());
        vo.setUserId(goods.getUserId());
        vo.setUserName(adminUserLookupService.buildUserDisplayNameMap(List.of(goods.getUserId())).get(goods.getUserId()));
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
        vo.setImageList(goodsInfoService.getDetail(null, id).getImageList());
        return Result.success(vo);
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@RequestHeader("Authorization") String authorization,
                                     @PathVariable Long id,
                                     @Valid @RequestBody AdminGoodsStatusDTO dto) {
        assertAdmin(authorization);

        GoodsInfo goods = goodsInfoService.getById(id);
        if (goods == null || (goods.getDeleted() != null && goods.getDeleted() == 1)) {
            throw new BusinessException(404, "商品不存在");
        }

        goods.setStatus(dto.getStatus().trim());
        goodsInfoService.updateById(goods);
        return Result.success("操作成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id) {
        assertAdmin(authorization);
        boolean removed = goodsInfoService.removeById(id);
        if (!removed) {
            throw new BusinessException(404, "商品不存在");
        }
        return Result.success("删除成功", null);
    }
}
