package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.entity.GoodsOrderLog;
import com.campus.resourcesharing.mapper.GoodsOrderLogMapper;
import com.campus.resourcesharing.service.GoodsOrderLogService;
import org.springframework.stereotype.Service;

@Service
public class GoodsOrderLogServiceImpl extends ServiceImpl<GoodsOrderLogMapper, GoodsOrderLog> implements GoodsOrderLogService {
}
