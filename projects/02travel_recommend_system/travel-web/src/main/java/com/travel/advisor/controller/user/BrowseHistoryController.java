package com.travel.advisor.controller.user;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.history.BrowseHistoryCreateDTO;
import com.travel.advisor.service.BrowseHistoryService;
import com.travel.advisor.vo.history.BrowseHistoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/browse-history")
@RequiredArgsConstructor
public class BrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    @PostMapping
    public Result<Void> report(@Valid @RequestBody BrowseHistoryCreateDTO dto) {
        browseHistoryService.report(dto);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<BrowseHistoryVO>> page(PageQuery pageQuery) {
        return Result.success(browseHistoryService.page(pageQuery));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        browseHistoryService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        browseHistoryService.clear();
        return Result.success();
    }
}
