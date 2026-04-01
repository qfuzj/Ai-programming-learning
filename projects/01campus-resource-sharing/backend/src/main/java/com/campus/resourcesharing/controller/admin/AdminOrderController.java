package com.campus.resourcesharing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.entity.GoodsOrder;
import com.campus.resourcesharing.query.admin.AdminOrderPageQuery;
import com.campus.resourcesharing.service.admin.AdminUserLookupService;
import com.campus.resourcesharing.service.GoodsOrderService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.admin.AdminOrderVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController extends AdminBaseController {

    private final GoodsOrderService goodsOrderService;
    private final AdminUserLookupService adminUserLookupService;

    public AdminOrderController(JwtUtil jwtUtil,
                                GoodsOrderService goodsOrderService,
                                AdminUserLookupService adminUserLookupService) {
        super(jwtUtil);
        this.goodsOrderService = goodsOrderService;
        this.adminUserLookupService = adminUserLookupService;
    }

    @GetMapping("/page")
    public Result<PageResult<AdminOrderVO>> page(@RequestHeader("Authorization") String authorization,
                                                 @Valid AdminOrderPageQuery query) {
        assertAdmin(authorization);

        boolean hasBuyerKeyword = query.getBuyerKeyword() != null && !query.getBuyerKeyword().isBlank();
        boolean hasSellerKeyword = query.getSellerKeyword() != null && !query.getSellerKeyword().isBlank();
        Set<Long> buyerIds = adminUserLookupService.findUserIdsByKeyword(query.getBuyerKeyword());
        Set<Long> sellerIds = adminUserLookupService.findUserIdsByKeyword(query.getSellerKeyword());

        if ((hasBuyerKeyword && buyerIds.isEmpty()) || (hasSellerKeyword && sellerIds.isEmpty())) {
            return Result.success(PageResult.empty(query.getPageNum(), query.getPageSize()));
        }

        LambdaQueryWrapper<GoodsOrder> wrapper = new LambdaQueryWrapper<GoodsOrder>()
                .like(query.getOrderNo() != null && !query.getOrderNo().isBlank(), GoodsOrder::getOrderNo, query.getOrderNo())
                .eq(query.getStatus() != null && !query.getStatus().isBlank(), GoodsOrder::getStatus, query.getStatus())
                .eq(query.getBuyerId() != null, GoodsOrder::getBuyerId, query.getBuyerId())
                .eq(query.getSellerId() != null, GoodsOrder::getSellerId, query.getSellerId())
                .in(hasBuyerKeyword, GoodsOrder::getBuyerId, buyerIds)
                .in(hasSellerKeyword, GoodsOrder::getSellerId, sellerIds)
                .orderByDesc(GoodsOrder::getCreateTime);

        Page<GoodsOrder> page = goodsOrderService.page(query.toPage(), wrapper);

        Set<Long> userIds = page.getRecords().stream()
                .flatMap(item -> List.of(item.getBuyerId(), item.getSellerId()).stream())
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = adminUserLookupService.buildUserDisplayNameMap(userIds);

        List<AdminOrderVO> records = page.getRecords().stream().map(item -> {
            AdminOrderVO vo = new AdminOrderVO();
            vo.setId(item.getId());
            vo.setOrderNo(item.getOrderNo());
            vo.setGoodsId(item.getGoodsId());
            vo.setBuyerId(item.getBuyerId());
            vo.setBuyerName(userNameMap.get(item.getBuyerId()));
            vo.setSellerId(item.getSellerId());
            vo.setSellerName(userNameMap.get(item.getSellerId()));
            vo.setDealPrice(item.getDealPrice());
            vo.setStatus(item.getStatus());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).toList();

        return Result.success(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), records));
    }

    @GetMapping("/detail/{id}")
    public Result<GoodsOrder> detail(@RequestHeader("Authorization") String authorization,
                                     @PathVariable Long id) {
        assertAdmin(authorization);
        GoodsOrder order = goodsOrderService.getById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return Result.success(order);
    }

    @PutMapping("/close/{id}")
    public Result<Void> close(@RequestHeader("Authorization") String authorization,
                              @PathVariable Long id) {
        assertAdmin(authorization);

        GoodsOrder order = goodsOrderService.getById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if ("completed".equals(order.getStatus())) {
            throw new BusinessException(400, "已完成订单不可关闭");
        }

        order.setStatus("closed");
        goodsOrderService.updateById(order);
        return Result.success("订单已关闭", null);
    }
}
