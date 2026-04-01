package com.campus.resourcesharing.controller;

import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.service.CategoryService;
import com.campus.resourcesharing.vo.home.CategoryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public Result<List<CategoryVO>> listCategories() {
        List<CategoryVO> categories = categoryService.listCategories();
        return Result.success("获取分类列表成功", categories);
    }
}
