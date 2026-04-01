package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.entity.GoodsCategory;
import com.campus.resourcesharing.mapper.GoodsCategoryMapper;
import com.campus.resourcesharing.service.GoodsCategoryService;
import org.springframework.stereotype.Service;

@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {
}
