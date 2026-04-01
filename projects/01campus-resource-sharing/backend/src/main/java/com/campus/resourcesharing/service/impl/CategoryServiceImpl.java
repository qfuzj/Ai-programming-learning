package com.campus.resourcesharing.service.impl;

import com.campus.resourcesharing.entity.GoodsCategory;
import com.campus.resourcesharing.service.CategoryService;
import com.campus.resourcesharing.service.GoodsCategoryService;
import com.campus.resourcesharing.vo.home.CategoryVO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final GoodsCategoryService goodsCategoryService;

    public CategoryServiceImpl(GoodsCategoryService goodsCategoryService) {
        this.goodsCategoryService = goodsCategoryService;
    }

    @Override
    public List<CategoryVO> listCategories() {
        List<GoodsCategory> categories = goodsCategoryService.list();
        return categories.stream()
                .filter(cat -> cat.getStatus() == 1)
                .map(cat -> new CategoryVO(cat.getId(), cat.getName(), cat.getSort()))
                .sorted((a, b) -> a.getSort().compareTo(b.getSort()))
                .collect(Collectors.toList());
    }
}
