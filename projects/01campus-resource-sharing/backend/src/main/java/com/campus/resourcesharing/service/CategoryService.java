package com.campus.resourcesharing.service;

import com.campus.resourcesharing.vo.home.CategoryVO;
import java.util.List;

public interface CategoryService {
    
    /**
     * 获取所有分类列表
     */
    List<CategoryVO> listCategories();
}
