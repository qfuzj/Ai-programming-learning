package com.travel.advisor.llm.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.GenerationUsage;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResponseFormat;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.protocol.ConnectionOptions;
import com.travel.advisor.dto.llm.LlmRequest;
import com.travel.advisor.dto.llm.LlmResponse;
import com.travel.advisor.llm.LlmGateway;
import com.travel.advisor.llm.LlmProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认的 LLM 网关实现，调用阿里 DashScope 获取真实回复。
 */
@Component
@RequiredArgsConstructor
public class DefaultLlmGateway implements LlmGateway {

    private final LlmProperties llmProperties;

    @Override
    public LlmResponse generate(LlmRequest request) {
        if (Boolean.FALSE.equals(llmProperties.getEnabled())) {
            throw new IllegalStateException("LLM 已禁用");
        }

        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未配置 DASHSCOPE_API_KEY");
        }

        try {
            Generation generation = new Generation(
                    "http",
                    llmProperties.getBaseUrl(),
                    buildConnectionOptions(request));
            GenerationResult result = generation.call(buildParam(request, apiKey));
            return buildResponse(result, request);
        } catch (ApiException | NoApiKeyException | InputRequiredException ex) {
            throw new IllegalStateException("DashScope 调用失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 根据通用请求构建 DashScope 参数。
     * 1. 从请求中提取消息列表，并转换为 DashScope 的 Message 对象列表
     * 2. 确定使用的模型名称，优先使用请求中指定的模型，若未指定则使用默认配置
     * 3. 设置最大 token 数量和结果格式，如果请求开启 JSON 模式，则设置响应格式为 JSON 对象
     * 4. 返回构建好的 GenerationParam 对象，供 DashScope 客户端调用使用
     */
    private GenerationParam buildParam(LlmRequest request, String apiKey) {
        List<Message> messages = new ArrayList<>();
        if (request.getMessages() != null) {
            request.getMessages().forEach(item -> messages.add(Message.builder()
                    .role(item.getRole())
                    .content(item.getContent())
                    .build()));
        }

        GenerationParam.GenerationParamBuilder<?, ?> builder = GenerationParam.builder()
                .apiKey(apiKey)
                .model(resolveModelName(request))
                .messages(messages)
                .maxTokens(llmProperties.getMaxTokens())
                .resultFormat(GenerationParam.ResultFormat.MESSAGE);

        if (Boolean.TRUE.equals(request.getJsonMode())) {
            builder.responseFormat(ResponseFormat.builder()
                    .type(ResponseFormat.JSON_OBJECT)
                    .build());
        }
        return builder.build();
    }

    /**
     * 根据请求构建 LLM 服务 的HTTP 连接配置
     *
     * <p>
     * 连接配置包括连接超时、读取超时和写入超时，这三个超时时间默认使用相同的值。
     * 超时时间的确定规则：优先使用请求中显示传入的 timeoutMs （需大于0），
     * 若请求未指定或值无效，则回退到配置文件中的默认超时时间。
     *
     * @param request LLM 聊天请求，可能包含特定的超时设置
     * @return ConnectionOptions 实例，封装了连接、读写超时配置，提供底层HTTP客户端使用
     */
    private ConnectionOptions buildConnectionOptions(LlmRequest request) {
        // 确定超时毫秒数
        int timeoutMs = request.getTimeoutMs() == null || request.getTimeoutMs() <= 0
                ? llmProperties.getTimeoutMs()
                : request.getTimeoutMs();
        // 将毫秒数转换为 Duration 对象，便于构建连接选项
        Duration timeout = Duration.ofMillis(timeoutMs);
        // 使用构建器模式创建 ConnectionOptions，三个方向的超时设置为相同的值
        return ConnectionOptions.builder()
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .build();
    }

    /**
     * 根据 GenerationResult 构建 LlmResponse 对象。
     * 1. 从 GenerationResult 中提取回复内容，优先从 choices 列表中的 message 获取，如果没有则尝试从 text 字段获取
     * 2. 确定使用的模型名称，优先使用 GenerationResult 中的输出模型名称，如果没有则回退到请求中指定的模型名称
     * 3. 从 GenerationResult 中提取 token 使用情况，包括输入 token 数量、输出 token 数量和总 token 数量
     * 4. 构建并返回 LlmResponse 对象，封装回复内容、模型名称和 token 使用情况，供上层业务使用
     *
     */
    private LlmResponse buildResponse(GenerationResult result, LlmRequest request) {
        String content = "";
        if (result.getOutput() != null && result.getOutput().getChoices() != null
                && !result.getOutput().getChoices().isEmpty()) {
            Message message = result.getOutput().getChoices().get(0).getMessage();
            if (message != null && message.getContent() != null) {
                content = message.getContent();
            }
        }
        if ((content == null || content.isBlank()) && result.getOutput() != null) {
            content = result.getOutput().getText();
        }

        GenerationUsage usage = result.getUsage();
        return LlmResponse.builder()
                .content(content == null ? "" : content)
                .modelName(result.getOutput() != null && result.getOutput().getModelName() != null
                        ? result.getOutput().getModelName()
                        : resolveModelName(request))
                .inputTokens(usage == null || usage.getInputTokens() == null ? 0 : usage.getInputTokens())
                .outputTokens(usage == null || usage.getOutputTokens() == null ? 0 : usage.getOutputTokens())
                .totalTokens(usage == null || usage.getTotalTokens() == null ? 0 : usage.getTotalTokens())
                .build();
    }

    /**
     * 获取模型名字
     */
    private String resolveModelName(LlmRequest request) {
        return request.getModelName() == null || request.getModelName().isBlank()
                ? llmProperties.getModelName()
                : request.getModelName();
    }

    // 优先从环境变量获取 API Key，兼容两种配置方式：环境变量和配置文件
    private String resolveApiKey() {
        String envApiKey = System.getenv("DASHSCOPE_API_KEY");
        if (envApiKey != null && !envApiKey.isBlank()) {
            return envApiKey;
        }
        return llmProperties.getApiKey();
    }
}
