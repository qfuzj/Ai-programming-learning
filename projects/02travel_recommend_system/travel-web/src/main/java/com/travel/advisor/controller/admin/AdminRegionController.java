package com.travel.advisor.controller.admin;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.region.RegionCreateDTO;
import com.travel.advisor.dto.region.RegionUpdateDTO;
import com.travel.advisor.entity.Region;
import com.travel.advisor.service.RegionService;
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
@RequestMapping("/api/admin/regions")
@RequiredArgsConstructor
public class AdminRegionController {

    private final RegionService regionService;

    @GetMapping
    public Result<PageResult<Region>> page(
            @Valid RegionCreateDTO dto,
            PageQuery pageQuery) {
        return Result.success(regionService.page(dto, pageQuery));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody RegionCreateDTO dto) {
        return Result.success(regionService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody RegionUpdateDTO dto) {
        regionService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        regionService.delete(id);
        return Result.success();
    }
}
