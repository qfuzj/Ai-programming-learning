package com.travel.advisor.service.impl;

import com.travel.advisor.common.enums.FileResourceStatus;
import com.travel.advisor.common.enums.FileResourceUploaderType;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.file.FileUploadCallbackDTO;
import com.travel.advisor.entity.FileResource;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.FileResourceMapper;
import com.travel.advisor.minio.MinioProperties;
import com.travel.advisor.service.FileService;
import com.travel.advisor.utils.BeanCopyUtils;
import com.travel.advisor.utils.SecurityUtils;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    // 允许的文件类型
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", // 图片
            "mp4", "avi", "mov" // 视频
    );

    // 最大文件大小 100MB
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024L;

    private final FileResourceMapper fileResourceMapper;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * 应用启动时应该初始化存储桶
     * 避免每次上传都检查桶是否存在
     */
    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .build());
            if (!exists) {
                // 创建桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .build());
                // 设置公开访问策略
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .config(buildBucketPolicy(minioProperties.getBucketName()))
                                .build());
                log.info("MinIO 存储桶 [{}] 创建成功", minioProperties.getBucketName());
            } else {
                log.info("MinIO 存储桶 [{}] 已存在，无需创建", minioProperties.getBucketName());
            }

        } catch (Exception e) {
            log.error("初始化 MinIO 存储桶失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "初始化存储桶失败: " + e.getMessage());
        }
    }

    /**
     * 获取预签名上传凭证
     * 前端拿到 uploadUrl 后直接 PUT 上传到 MinIO，无需经过后端
     *
     * @param bizType  业务类型 （如 avatar、scenic、review）
     * @param fileName 原始文件名，用于提取扩展名
     * @param bizId    业务ID，可选参数，用于关联具体业务数据
     * @return 包含上传所需信息的 Map，包含
     * bucketName、objectKey、uploadUrl、method、expireAt、bizType、bizId 和
     * callbackUrl 等字段
     */
    @Override
    public Map<String, Object> getUploadToken(String bizType, String fileName, Long fileSize, Long bizId) {

        validateFileName(fileName, fileSize);

        String normalizedBizType = StringUtils.hasText(bizType) ? bizType : "default";
        String objectKey = buildObjectKey(normalizedBizType, fileName);

        try {
            // 生成预签名上传URL （15分钟有效）
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .method(Method.PUT)
                            .expiry(60, TimeUnit.MINUTES)
                            .build());
            LinkedHashMap<String, Object> token = new LinkedHashMap<>();
            token.put("bucketName", minioProperties.getBucketName());
            token.put("objectKey", objectKey);
            token.put("uploadUrl", presignedUrl);
            token.put("method", "PUT");
            token.put("expireAt", LocalDateTime.now().plusMinutes(60));
            token.put("bizType", normalizedBizType);
            token.put("bizId", bizId);
            token.put("callbackUrl", "/api/common/files/callback");
            // 上传成功后可访问的文件URL
            token.put("url", buildFileUrl(objectKey));
            return token;
        } catch (Exception e) {
            log.error("生成预签名URL失败，objectKey={}", objectKey, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "获取上传凭证失败" + e.getMessage());
        }
    }

    /**
     * 验证文件名和文件大小是否合法
     */
    private void validateFileName(String fileName, Long fileSize) {
        // 校验文件名
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名不合法");
        }

        // 校验文件类型
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "不支持的文件类型: " + ext + "，支持: " + ALLOWED_EXTENSIONS);
        }

        // 校验文件大小
        if (fileSize != null && fileSize > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "文件大小超出限制，最大支持 100MB");
        }
    }

    /**
     * 文件上传回调接口
     * 前端上传完后调用此接口，将文件信息记录到数据库中
     *
     * @param dto 回调数据
     * @return 文件记录ID
     */
    @Override
    public Long uploadCallback(FileUploadCallbackDTO dto) {

        // 验证文件是否真实存在于MinIO中
        StatObjectResponse statObject = verifyAndStatObject(dto.getBucketName(), dto.getObjectKey());

        FileResource entity = BeanCopyUtils.copy(dto, FileResource.class);

        entity.setFileSize(statObject.size());
        entity.setFileType(statObject.contentType());
        entity.setFileExtension(extractExtension(dto.getObjectKey()));
        entity.setFileHash(statObject.etag());
        entity.setUrl(buildFileUrl(dto.getObjectKey()));
        entity.setUploaderId(SecurityUtils.getCurrentUserId());
        entity.setUploaderType(FileResourceUploaderType.fromRoleType(SecurityUtils.getCurrentRoleType()).getCode());
        entity.setStatus(FileResourceStatus.NORMAL.getCode());

        fileResourceMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public FileResource getById(Long id) {
        FileResource fileResource = fileResourceMapper.selectById(id);
        if (fileResource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        return fileResource;
    }

    /**
     * 根据ID删除文件
     * 同时删除数据库记录和 MinIO 中的实际文件
     */
    @Override
    public void deleteById(Long id) {
        FileResource fileResource = fileResourceMapper.selectById(id);
        if (fileResource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }

        // 删除MinIO中的实际文件
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileResource.getObjectKey())
                            .build());
            log.info("MinIO文件删除成功，objectKey={}", fileResource.getObjectKey());
        } catch (Exception e) {
            log.info("MinIO文件删除失败，objectKey={}", fileResource.getObjectKey(), e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件删除失败: " + e.getMessage());
        }
        // 删除数据库记录
        fileResourceMapper.deleteById(id);
        log.info("数据记录删除成功，id={}", id);
    }

    /**
     * 验证文件是否真实存在于 MinIO 中
     */
    private StatObjectResponse verifyAndStatObject(String bucketName, String objectKey) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
        } catch (Exception e) {
            log.warn("文件在MinIO中不存在，bucket={}, objectKey={}", bucketName, objectKey);
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件未上传成功或不存在");
        }
    }

    /**
     * 构建文件访问URL
     */
    private String buildFileUrl(String objectKey) {
        return String.join("/",
                minioProperties.getEndpoint(), // http://172.16.219.100:9000
                minioProperties.getBucketName(), // travel-advisor
                objectKey // avatar/2026-04-01/uuid.jpg
        );
    }

    /**
     * 从 objectKey 中提取原始文件名（如果 originalName 不传，则使用 objectKey 的最后一部分作为文件名）
     */
    private String extractFileName(String objectKey) {
        return objectKey.substring(objectKey.lastIndexOf('/') + 1);
    }

    /**
     * 从 objectKey 中提取文件扩展名
     */
    private String extractExtension(String objectKey) {

        return objectKey.substring(objectKey.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 构建对象存储的 ObjectKey，格式为：{bizType}/{yyyy-MM-dd}/{uuid}{ext}
     *
     * @param bizType  业务类型
     * @param fileName 原始文件名，用于提取扩展名
     * @return 构建好的 ObjectKey
     */
    private String buildObjectKey(String bizType, String fileName) {
        String ext = "";
        if (StringUtils.hasText(fileName) && fileName.contains(".")) {
            ext = fileName.substring(fileName.lastIndexOf('.')); // -> ".jpg"
        }
        return bizType + "/" + LocalDate.now() + "/"
                + UUID.randomUUID().toString().replace("-", "") + ext;
    }

    /**
     * 构建存储桶访问策略 （仅允许匿名GET）
     */
    private String buildBucketPolicy(String bucketName) {
        return """
                {
                  "Statement" : [ {
                    "Action" : "s3:GetObject",
                    "Effect" : "Allow",
                    "Principal" : "*",
                    "Resource" : "arn:aws:s3:::%s/*"
                  } ],
                  "Version" : "2012-10-17"
                }
                """.formatted(bucketName);
    }
}
