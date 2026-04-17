package com.travel.advisor.service;

import com.travel.advisor.common.enums.BizType;
import com.travel.advisor.dto.file.FileUploadCallbackDTO;
import com.travel.advisor.entity.FileResource;

import java.util.List;
import java.util.Map;

public interface FileService {

    /**
     * 获取上传凭证（预留）
     */
    Map<String, Object> getUploadToken(String bizType, String fileName, Long fileSize, Long bizId);

    /**
     * 上传回调登记
     */
    Long uploadCallback(FileUploadCallbackDTO dto);

    /**
     * 查询文件元数据
     */
    FileResource getById(Long id);

    /**
     * 删除文件引用（逻辑删除）
     */
    void deleteById(Long id);

    /**
     * 绑定临时文件到业务数据
     */
    void bindFilesToBiz(List<Long> fileIds, Long bizId, BizType bizType);

    /**
     * 批量解除业务图片绑定并标记已删除
     */
    void deleteFilesByBiz(Long bizId, BizType bizType);

    /**
     * 清理过期临时文件（物理删除/逻辑删除，并清理对象存储）
     */
    int cleanupTempFiles();
}
