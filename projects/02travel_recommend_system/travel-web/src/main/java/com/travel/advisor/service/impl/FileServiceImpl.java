package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.travel.advisor.common.enums.BizType;
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
import java.util.List;
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
        entity.setStatus(FileResourceStatus.TEMP.getCode());

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
        // 只更新状态为已删除
        FileResource updateEntity = new FileResource();
        updateEntity.setId(id);
        updateEntity.setStatus(FileResourceStatus.DELETED.getCode());
        fileResourceMapper.updateById(updateEntity);
        log.info("数据记录已标记为删除状态，id={}", id);
    }

    /**
     * 将多个文件与某个业务（如订单、用户等）绑定，并更新文件的状态。
      - 前端上传文件后会得到一个文件ID列表，调用此接口将这些文件与具体业务数据关联起来
      - 更新文件状态为已使用，并记录使用时间
     */
    @Override
    public void bindFilesToBiz(java.util.List<Long> fileIds, Long bizId, BizType bizType) {
        if (fileIds == null || fileIds.isEmpty()) return;
        LambdaUpdateWrapper<FileResource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(FileResource::getId, fileIds)
                .set(FileResource::getBizId, bizId)
                .set(FileResource::getBizType, bizType.name())
                .set(FileResource::getStatus, FileResourceStatus.USED.getCode())
                .set(FileResource::getUsedTime, LocalDateTime.now());
        fileResourceMapper.update(null, wrapper);
    }

    /**
     * 删除与特定业务（如订单、用户等）绑定的文件。(更新文件的状态为 "已删除"，而不是物理删除。)
     */
    @Override
    public void deleteFilesByBiz(Long bizId, BizType bizType) {
        LambdaUpdateWrapper<FileResource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FileResource::getBizId, bizId)
                .eq(FileResource::getBizType, bizType.name())
                .set(FileResource::getStatus, FileResourceStatus.DELETED.getCode());
        fileResourceMapper.update(null, wrapper);
    }

    /**
     * 清理过期的临时文件
      - 定时任务调用，清理24小时内未被绑定到业务的临时文件
      - 删除MinIO中的实际文件，并将数据库记录状态更新为已删除
     */
    @Override
    public int cleanupTempFiles() {
        // 定义过期时间阈值，24小时之前的文件视为过期
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        LambdaQueryWrapper<FileResource> query = new LambdaQueryWrapper<>();
        query.eq(FileResource::getStatus, FileResourceStatus.TEMP.getCode())
                .le(FileResource::getCreateTime, threshold);

        List<FileResource> tempFiles = fileResourceMapper.selectList(query);
        if (tempFiles.isEmpty()) return 0;

        int count = 0;
        for (FileResource file : tempFiles) {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .object(file.getObjectKey())
                                .build());
                
                FileResource updateEntity = new FileResource();
                updateEntity.setId(file.getId());
                updateEntity.setStatus(FileResourceStatus.DELETED.getCode());
                fileResourceMapper.updateById(updateEntity);
                
                count++;
            } catch (Exception e) {
                log.warn("清理临时文件失败 id={}", file.getId(), e);
            }
        }
        return count;
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
