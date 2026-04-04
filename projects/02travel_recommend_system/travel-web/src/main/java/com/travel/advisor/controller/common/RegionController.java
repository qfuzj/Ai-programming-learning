package com.travel.advisor.controller.common;

import com.travel.advisor.common.result.Result;
import com.travel.advisor.service.RegionService;
import com.travel.advisor.vo.region.RegionTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/common/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/tree")
    public Result<List<RegionTreeVO>> getTree() {
        return Result.success(regionService.getTree());
    }
}
