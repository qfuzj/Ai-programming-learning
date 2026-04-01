package com.campus.resourcesharing.controller.user;

import com.campus.resourcesharing.common.query.PageQuery;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.order.OrderCreateDTO;
import com.campus.resourcesharing.service.GoodsOrderService;
import com.campus.resourcesharing.vo.order.OrderDetailVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final GoodsOrderService goodsOrderService;

    public OrderController(GoodsOrderService goodsOrderService) {
        this.goodsOrderService = goodsOrderService;
    }

    @PostMapping("/create")
    public Result<Map<String, Long>> create(@RequestHeader("Authorization") String authorization,
                                            @Valid @RequestBody OrderCreateDTO dto) {
        Long id = goodsOrderService.createOrder(extractToken(authorization), dto);
        return Result.success("下单成功", Map.of("id", id));
    }

    @GetMapping("/buyer/list")
    public Result<PageResult<OrderDetailVO>> buyerList(@RequestHeader("Authorization") String authorization,
                                                        @Valid PageQuery query) {
        return Result.success(goodsOrderService.buyerOrders(extractToken(authorization), query));
    }

    @GetMapping("/seller/list")
    public Result<PageResult<OrderDetailVO>> sellerList(@RequestHeader("Authorization") String authorization,
                                                         @Valid PageQuery query) {
        return Result.success(goodsOrderService.sellerOrders(extractToken(authorization), query));
    }

    @GetMapping("/detail/{id}")
    public Result<OrderDetailVO> detail(@RequestHeader("Authorization") String authorization,
                                        @PathVariable Long id) {
        return Result.success(goodsOrderService.orderDetail(extractToken(authorization), id));
    }

    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id) {
        goodsOrderService.cancelOrder(extractToken(authorization), id);
        return Result.success("取消成功", null);
    }

    @PutMapping("/confirm/{id}")
    public Result<Void> confirm(@RequestHeader("Authorization") String authorization,
                                @PathVariable Long id) {
        goodsOrderService.confirmOrder(extractToken(authorization), id);
        return Result.success("确认成功", null);
    }

    @PutMapping("/complete/{id}")
    public Result<Void> complete(@RequestHeader("Authorization") String authorization,
                                 @PathVariable Long id) {
        goodsOrderService.completeOrder(extractToken(authorization), id);
        return Result.success("完成成功", null);
    }

    private String extractToken(String authorization) {
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }
}
