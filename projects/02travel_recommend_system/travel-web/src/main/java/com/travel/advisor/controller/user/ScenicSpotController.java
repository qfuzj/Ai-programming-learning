package com.travel.advisor.controller.user;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.scenic.ScenicQueryDTO;
import com.travel.advisor.service.ScenicSpotService;
import com.travel.advisor.vo.scenic.ScenicDetailVO;
import com.travel.advisor.vo.scenic.ScenicFilterOptionsVO;
import com.travel.advisor.vo.scenic.ScenicListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/scenic-spots")
@RequiredArgsConstructor
public class ScenicSpotController {

    private final ScenicSpotService scenicSpotService;

    @GetMapping
    public Result<PageResult<ScenicListVO>> page(@Valid ScenicQueryDTO dto) {
        dto.setStatus(1);
        return Result.success(scenicSpotService.page(dto));
    }

    @GetMapping("/{id}")
    public Result<ScenicDetailVO> detail(@PathVariable Long id) {
        return Result.success(scenicSpotService.detail(id));
    }

    @GetMapping("/hot")
    public Result<List<ScenicListVO>> hot() {
        return Result.success(scenicSpotService.hotList());
    }

    @GetMapping("/filter-options")
    public Result<ScenicFilterOptionsVO> filterOptions() {
        return Result.success(scenicSpotService.filterOptions());
    }
}