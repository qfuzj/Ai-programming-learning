package com.campus.resourcesharing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.admin.AdminCategorySaveDTO;
import com.campus.resourcesharing.entity.GoodsCategory;
import com.campus.resourcesharing.service.GoodsCategoryService;
import com.campus.resourcesharing.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category")
public class AdminCategoryController extends AdminBaseController {

    private final GoodsCategoryService goodsCategoryService;

    public AdminCategoryController(JwtUtil jwtUtil, GoodsCategoryService goodsCategoryService) {
        super(jwtUtil);
        this.goodsCategoryService = goodsCategoryService;
    }

    @GetMapping("/list")
    public Result<List<GoodsCategory>> list(@RequestHeader("Authorization") String authorization) {
        assertAdmin(authorization);
        List<GoodsCategory> list = goodsCategoryService.list(new LambdaQueryWrapper<GoodsCategory>()
                .orderByAsc(GoodsCategory::getSort)
                .orderByDesc(GoodsCategory::getCreateTime));
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestHeader("Authorization") String authorization,
                            @Valid @RequestBody AdminCategorySaveDTO dto) {
        assertAdmin(authorization);

        long count = goodsCategoryService.count(new LambdaQueryWrapper<GoodsCategory>()
                .eq(GoodsCategory::getName, dto.getName().trim()));
        if (count > 0) {
            throw new BusinessException(400, "分类名称已存在");
        }

        GoodsCategory category = new GoodsCategory();
        category.setName(dto.getName().trim());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        goodsCategoryService.save(category);
        return Result.success("新增成功", null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestHeader("Authorization") String authorization,
                               @Valid @RequestBody AdminCategorySaveDTO dto) {
        assertAdmin(authorization);

        if (dto.getId() == null) {
            throw new BusinessException(400, "id不能为空");
        }

        GoodsCategory category = goodsCategoryService.getById(dto.getId());
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        long count = goodsCategoryService.count(new LambdaQueryWrapper<GoodsCategory>()
                .eq(GoodsCategory::getName, dto.getName().trim())
                .ne(GoodsCategory::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException(400, "分类名称已存在");
        }

        category.setName(dto.getName().trim());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        goodsCategoryService.updateById(category);
        return Result.success("更新成功", null);
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@RequestHeader("Authorization") String authorization,
                                     @PathVariable Long id,
                                     @RequestParam Integer status) {
        assertAdmin(authorization);

        GoodsCategory category = goodsCategoryService.getById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        category.setStatus(status);
        goodsCategoryService.updateById(category);
        return Result.success("操作成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id) {
        assertAdmin(authorization);
        goodsCategoryService.removeById(id);
        return Result.success("删除成功", null);
    }
}
