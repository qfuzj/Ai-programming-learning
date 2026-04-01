package com.campus.resourcesharing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.resourcesharing.common.query.PageQuery;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.dto.order.OrderCreateDTO;
import com.campus.resourcesharing.entity.GoodsOrder;
import com.campus.resourcesharing.vo.order.OrderDetailVO;

public interface GoodsOrderService extends IService<GoodsOrder> {

	Long createOrder(String token, OrderCreateDTO dto);

	PageResult<OrderDetailVO> buyerOrders(String token, PageQuery query);

	PageResult<OrderDetailVO> sellerOrders(String token, PageQuery query);

	OrderDetailVO orderDetail(String token, Long id);

	void cancelOrder(String token, Long id);

	void confirmOrder(String token, Long id);

	void completeOrder(String token, Long id);
}
