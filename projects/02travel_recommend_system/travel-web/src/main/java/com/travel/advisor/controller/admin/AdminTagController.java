package com.travel.advisor.controller.admin;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.tag.TagCreateDTO;
import com.travel.advisor.dto.tag.TagQueryDTO;
import com.travel.advisor.dto.tag.TagUpdateDTO;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.annotation.OperationLog;
import com.travel.advisor.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @GetMapping
    public Result<PageResult<Tag>> page(
            TagQueryDTO dto,
            PageQuery pageQuery) {
        return Result.success(tagService.page(dto, pageQuery));
    }

    @GetMapping("/categories")
    public Result<List<String>> listCategories(@RequestParam(required = false) String scope) {
        return Result.success(tagService.listCategoriesByScope(scope));
    }

    @PostMapping
    @OperationLog(module = "tag", action = "create", description = "新增标签")
    public Result<Long> create(@Valid @RequestBody TagCreateDTO dto) {
        return Result.success(tagService.create(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "tag", action = "update", description = "更新标签")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody TagUpdateDTO dto) {
        tagService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "tag", action = "delete", description = "删除标签")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
