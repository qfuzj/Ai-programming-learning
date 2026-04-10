package com.travel.advisor.controller.common;

import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.file.FileUploadCallbackDTO;
import com.travel.advisor.entity.FileResource;
import com.travel.advisor.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/common/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 获取上传凭证（预留）
     */
    @PostMapping("/upload-token")
    public Result<Map<String, Object>> getUploadToken(@RequestParam(required = false) String bizType,
                                                      @RequestParam(required = false) String fileName,
                                                      @RequestParam(required = false) Long fileSize,
                                                      @RequestParam(required = false) Long bizId) {
        return Result.success(fileService.getUploadToken(bizType, fileName, fileSize, bizId));
    }

    /**
     * 上传回调登记
     */
    @PostMapping("/callback")
    public Result<Long> uploadCallback(@Valid @RequestBody FileUploadCallbackDTO dto) {
        return Result.success(fileService.uploadCallback(dto));
    }

    /**
     * 查询文件元数据
     */
    @GetMapping("/{id}")
    public Result<FileResource> getById(@PathVariable Long id) {
        return Result.success(fileService.getById(id));
    }

    /**
     * 删除文件引用
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        fileService.deleteById(id);
        return Result.success();
    }
}
