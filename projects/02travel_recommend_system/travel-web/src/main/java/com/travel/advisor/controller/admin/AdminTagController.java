package com.travel.advisor.controller.admin;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.tag.TagCreateDTO;
import com.travel.advisor.dto.tag.TagQueryDTO;
import com.travel.advisor.dto.tag.TagUpdateDTO;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    public Result<Long> create(@Valid @RequestBody TagCreateDTO dto) {
        return Result.success(tagService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody TagUpdateDTO dto) {
        tagService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
