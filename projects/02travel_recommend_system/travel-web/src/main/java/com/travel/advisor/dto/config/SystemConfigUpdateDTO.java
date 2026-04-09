package com.travel.advisor.dto.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SystemConfigUpdateDTO {

    /**
     * 配置值
     */
    @NotBlank(message = "配置值不能为空")
    private String configValue;

    /**
     * 值类型 （string / number / boolean / json）默认为string
     */
    @NotBlank(message = "配置类型不能为空")
    private String configType;

    /**
     * 配置分组 （basic / llm / recommend / minio 等）默认为default
     */
    @NotBlank(message = "配置分组不能为空")
    private String configGroup;

    /**
     * 配置说明
     */
    private String description;

    /**
     * 前端是否可见（0-不可见，1-可见），默认为0
     */
    @NotNull(message = "是否前端可见不能为空")
    private Integer isPublic;
}
