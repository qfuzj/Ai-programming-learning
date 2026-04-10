package com.travel.advisor.service;

import com.travel.advisor.dto.file.FileUploadCallbackDTO;
import com.travel.advisor.entity.FileResource;

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
}
