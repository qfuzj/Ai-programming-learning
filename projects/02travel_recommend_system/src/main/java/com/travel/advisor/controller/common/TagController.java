package com.travel.advisor.controller.common;

import com.travel.advisor.common.result.Result;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/common/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<Tag>> list(@RequestParam(required = false) Integer type) {
        return Result.success(tagService.listByType(type));
    }
}
