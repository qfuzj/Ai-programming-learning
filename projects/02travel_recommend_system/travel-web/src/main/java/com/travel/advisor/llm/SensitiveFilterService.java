package com.travel.advisor.llm;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 敏感词过滤服务，支持关键词匹配和正则匹配两种方式。
 * 当前为骨架实现，词库硬编码，后续可扩展为数据库/配置文件动态加载。
 */
@Component
public class SensitiveFilterService {

    // 后续扩展方向：
    // 1. 从数据库加载，支持动态更新
    //  @Autowired
    // private SensitiveWordMapper sensitiveWordMapper;

    // 2. 从配置文件加载
    // @ConfigurationProperties(prefix = "sensitive")
    //private List<String> blockedTerms;

    // 3. 接入第三方审核 API（阿里云/腾讯云内容安全）

    /**
     * 关键词黑名单，精确包含匹配
     */
    private static final List<String> BLOCKED_TERMS = List.of("违禁词示例");
    /**
     * 正则黑名单，匹配敏感词片段
     */
    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
            Pattern.compile(".*(暴恐|涉毒|极端主义).*", Pattern.CASE_INSENSITIVE)
    );

    /**
     * 检测文本是否包含敏感内容。
     */
    public boolean isSensitive(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        // 关键词匹配
        if (BLOCKED_TERMS.stream().anyMatch(text::contains)) {
            return true;
        }
        return BLOCKED_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(text).find());
    }

    /**
     * 对敏感内容进行脱敏替换。
     */
    public String filter(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String sanitized = text;
        // 关键词替换
        for (String term : BLOCKED_TERMS) {
            sanitized = sanitized.replace(term, "***");
        }
        for (Pattern pattern : BLOCKED_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("***");
        }
        return sanitized;
    }

}
