package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("operation_log")
public class OperationLogE {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作员 ID
     */
    private Long adminUserId;

    /**
     * 操作员用户名
     */
    private String adminUsername;

    /**
     * 操作模块 （scenic / user / review / config 等）
     */
    private String module;

    /**
     * 操作类型 （create / update / delete / audit / export 等）
     */
    private String action;

    /**
     * 操作描述
     */
    private String description;

    /**
     * HTTP 请求方法
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数 (JSON,较大时截断)
     */
    private String requestParams;

    /**
     * 响应数据 (可选，较大时截断)
     */
    private String responseData;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 执行耗时（毫秒）
     */
    private Integer executionTimeMs;

    /**
     * 状态（0-失败，1-成功）
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    private LocalDateTime createTime;
}
