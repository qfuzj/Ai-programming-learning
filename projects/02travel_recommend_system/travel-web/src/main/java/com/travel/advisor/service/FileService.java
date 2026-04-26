package com.travel.advisor.service;

import com.travel.advisor.common.enums.BizType;
import com.travel.advisor.dto.file.FileUploadCallbackDTO;
import com.travel.advisor.entity.FileResource;

import java.util.Collection;
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

    /**
     * 批量解析文件资源 ID → 可访问 URL 映射。
     * <p>常用于 VO 拼装阶段把存储的 fileResourceId 替换为前端可直接渲染的 URL，
     * 避免在循环中逐条 selectById 触发 N+1。空入参返回空 Map。
     *
     * @param fileIds 文件资源主键集合（允许 null/空）
     * @return id → url 映射；URL 为空的资源会被过滤
     */
    Map<Long, String> resolveUrls(Collection<Long> fileIds);
}
