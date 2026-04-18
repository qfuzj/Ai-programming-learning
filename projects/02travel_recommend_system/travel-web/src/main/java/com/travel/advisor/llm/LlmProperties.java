package com.travel.advisor.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM配置属性类，用于从配置文件中加载与大语言模型相关的配置信息，如提供者、模型名称和最大Token数量等。
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private Boolean enabled = true;

    private String provider = "aliyun-dashscope";

    private String modelName = "qwen-plus";

    private String apiKey;

    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1";

    private Integer maxTokens = 2048;

    private Integer timeoutMs = 5000;
}
