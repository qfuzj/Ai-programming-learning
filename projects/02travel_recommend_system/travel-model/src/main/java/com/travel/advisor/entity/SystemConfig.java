package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体
 */
@Data
@TableName("system_config")
public class SystemConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键（唯一）
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 值类型 （string / number / boolean / json）默认为string
     */
    private String configType;

    /**
     * 配置分组 （basic / llm / recommend / minio 等）默认为default
     */
    private String configGroup;

    /**
     * 配置说明
     */
    private String description;

    /**
     * 前端是否可见（0-不可见，1-可见），默认为0
     */
    private Integer isPublic;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
