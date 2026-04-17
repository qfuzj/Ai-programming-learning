package com.travel.advisor.controller.admin;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.scenic.ScenicCreateDTO;
import com.travel.advisor.dto.scenic.ScenicImageCreateDTO;
import com.travel.advisor.dto.scenic.ScenicQueryDTO;
import com.travel.advisor.dto.scenic.ScenicStatusDTO;
import com.travel.advisor.dto.scenic.ScenicUpdateDTO;
import com.travel.advisor.service.ScenicSpotService;
import com.travel.advisor.vo.scenic.ScenicImageVO;
import com.travel.advisor.vo.scenic.ScenicListVO;
import com.travel.advisor.vo.scenic.ScenicDetailVO;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/scenic-spots")
@RequiredArgsConstructor
public class AdminScenicSpotController {

    private final ScenicSpotService scenicSpotService;

    @GetMapping
    public Result<PageResult<ScenicListVO>> page(@Valid ScenicQueryDTO dto) {
        return Result.success(scenicSpotService.page(dto));
    }

    @GetMapping("/{id}")
    public Result<ScenicDetailVO> detail(@PathVariable Long id) {
        return Result.success(scenicSpotService.adminDetail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ScenicCreateDTO dto) {
        return Result.success(scenicSpotService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ScenicUpdateDTO dto) {
        scenicSpotService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scenicSpotService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ScenicStatusDTO dto) {
        scenicSpotService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(@Valid @RequestBody ScenicStatusDTO dto) {
        scenicSpotService.batchUpdateStatus(dto);
        return Result.success();
    }

    @PutMapping("/{id}/tags")
    public Result<Void> updateTags(@PathVariable Long id, @RequestBody List<Long> tagIds) {
        scenicSpotService.updateTags(id, tagIds);
        return Result.success();
    }

    @GetMapping("/{id}/images")
    public Result<List<ScenicImageVO>> listImages(@PathVariable Long id) {
        return Result.success(scenicSpotService.listImages(id));
    }

    @PostMapping("/{id}/images")
    public Result<Long> addImage(@PathVariable Long id, @Valid @RequestBody ScenicImageCreateDTO dto) {
        return Result.success(scenicSpotService.addImage(id, dto));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public Result<Void> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        scenicSpotService.deleteImage(id, imageId);
        return Result.success();
    }
}
