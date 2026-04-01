package com.campus.resourcesharing.controller.common;

import com.campus.resourcesharing.common.exception.BusinessException;
import com.campus.resourcesharing.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "图片不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "仅支持图片文件");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(400, "图片大小不能超过5MB");
        }

        String originalName = file.getOriginalFilename();
        String extension = ".jpg";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.')).trim().replaceAll("\\s+", "");
            if (extension.isEmpty() || !extension.startsWith(".")) {
                extension = ".jpg";
            }
        }

        String fileName = "goods_" + UUID.randomUUID() + extension;
        File dir = new File("upload" + File.separator + "goods").getAbsoluteFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException(500, "创建上传目录失败");
        }

        File target = new File(dir, fileName).getAbsoluteFile();
        try {
            Files.copy(file.getInputStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(500, "上传失败");
        }

        String imageUrl = "/upload/goods/" + fileName;
        return Result.success("上传成功", Map.of("imageUrl", imageUrl));
    }
}
