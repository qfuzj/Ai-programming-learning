package com.travel.advisor.vo.log;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志列表 VO（仅包含列表页所需字段）
 */
@Data
public class OperationLogListVO {
    /**
     * 日志主键 ID
     */
    private Long id;

    /**
     * 操作模块
     * <p>
     * 例如：用户管理、内容发布、系统配置等
     */
    private String module;

    /**
     * 操作动作类型
     * <p>
     * 例如：CREATE (新增), UPDATE (修改), DELETE (删除), LOGIN (登录)
     */
    private String action;

    /**
     * 操作人员用户名
     * <p>
     * 记录执行该操作的管理人员登录账号
     */
    private String adminUsername;

    /**
     * 操作描述
     * <p>
     * 具体的业务操作说明，如：“修改了用户 [ID: 1001] 的权限状态”
     */
    private String description;

    /**
     * 请求接口 URL
     * <p>
     * 当前操作调用的 API 路径
     */
    private String requestUrl;

    /**
     * 方法执行耗时（毫秒）
     * <p>
     * 用于评估系统性能，定位慢接口
     */
    private Integer executionTimeMs;

    /**
     * 操作状态
     * <p>
     * 1: 成功, 0: 失败
     */
    private Integer status;

    /**
     * 操作发生时间（日志创建时间）
     */
    private LocalDateTime createdAt;
}
