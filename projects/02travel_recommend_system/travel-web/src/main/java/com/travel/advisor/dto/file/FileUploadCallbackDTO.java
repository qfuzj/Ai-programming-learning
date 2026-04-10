package com.travel.advisor.dto.file;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileUploadCallbackDTO {

    /**
     * 文件存储桶名称
     */
    @NotBlank(message = "bucketName 不能为空")
    private String bucketName;

    /**
     * 文件在存储桶中的对象键（路径）
     * 例如: "avatar/2026-04-01/uuid.jpg"
     */
    @NotBlank(message = "objectKey 不能为空")
    private String objectKey;

    /**
     * 文件原始名称，用户上传时的真实文件名
     * 例如: "我的头像.jpg"
     */
    @NotBlank(message = "originalName 不能为空")
    private String originalName;

    /**
     * 业务类型（avatar / scenic / review 等）
     */
    @NotBlank(message = "bizType 不能为空")
    private String bizType;

    /**
     * 业务ID，选填
     * 有些场景上传时还不知道关联的业务ID，后续可通过接口更新
     */
    private Long bizId;

    /**
     * 缩略图URL，选填
     * 图片类型文件才有，由前端或其他服务生成后传入
     */
    private String thumbnailUrl;
}
