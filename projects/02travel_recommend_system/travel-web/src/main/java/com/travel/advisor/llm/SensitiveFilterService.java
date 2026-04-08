package com.travel.advisor.llm;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 敏感词过滤骨架实现。
 */
@Component
public class SensitiveFilterService {

    private static final List<String> DEFAULT_BLOCKED_TERMS = List.of("违禁词示例");

    public boolean isSensitive(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        return DEFAULT_BLOCKED_TERMS.stream().anyMatch(text::contains);
    }

    public String sanitize(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String sanitized = text;
        for (String term : DEFAULT_BLOCKED_TERMS) {
            sanitized = sanitized.replace(term, "***");
        }
        return sanitized;
    }
}
