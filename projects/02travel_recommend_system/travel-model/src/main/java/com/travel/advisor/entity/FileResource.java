package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件资源实体
 */
@Data
@TableName("file_resource")
public class FileResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * MinIO bucket 名称
     */
    private String bucketName;

    /**
     * 对象存储中的对象键（路径），例如 "scenic/12345/photo.jpg"
     */
    private String objectKey;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件大小，单位字节
     */
    private Long fileSize;

    /**
     * 文件类型，例如 "image/jpeg"、"application/pdf" 等 MIME 类型
     */
    private String fileType;

    /**
     * 文件扩展名，例如 "jpg"、"png"、"pdf" 等（不带点）
     */
    private String fileExtension;

    /**
     * 文件 MD5 哈希值，用于文件完整性校验和去重
     */
    private String fileHash;

    /**
     * 文件访问 URL，通常是 MinIO 生成的预签名 URL 或者 CDN 加速后的 URL
     */
    private String url;

    /**
     * 缩略图 URL，如果是图片文件，可以生成一个缩略图并存储其访问 URL
     */
    private String thumbnailUrl;

    /**
     * 业务类型（avatar / scenic / review 等）
     */
    private String bizType;

    /**
     * 业务ID 反向关联
     */
    private Long bizId;

    /**
     * 上传者ID，关联用户表的用户ID，表示是谁上传了这个文件
     */
    private Long uploaderId;

    /**
     * 上传者类型：1 用户 2 管理员 3 系统
     */
    private Integer uploaderType;

    /**
     * 状态：0 待处理 1 正常 2 删除中
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
