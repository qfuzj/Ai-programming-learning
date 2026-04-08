package com.travel.advisor.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.ScenicSpotTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 负责管理对话上下文信息，提供合并和构建系统提示的方法。
 */
@Component
@RequiredArgsConstructor
public class ConversationContextManager {

    private final ObjectMapper objectMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;


    /**
     * @return 合并后的上下文信息（JSON字符串）。
     * - scenicId 为 null：直接返回 rawContextData
     * - scenicId 不为 null：在原始数据基础上追加 currentScenicId 和 currentScenic 字段
     * - 降级情况：景点查询失败时仅追加 currentScenicId；序列化失败时返回最小JSON
     */
    public String mergeContext(String rawContextData, Long scenicId) {
        // scenicId 为空，不需要合并，直接返回原始数据
        if (scenicId == null) {
            return rawContextData;
        }

        // 第一段：景点查询与上下文构建，失败时仅降级为不注入 currentScenic。
        ObjectNode scenicContextNode = null;
        try {
            scenicContextNode = buildScenicContextNode(scenicId);
        } catch (Exception ignored) {
            // 降级：景点查询失败，scenicContextNode 保持 null
//            scenicContextNode = null;
        }

        // 第二段：解析原始上下文，非法 JSON 时丢弃原始数据但保留景点上下文。
        ObjectNode mergedNode = objectMapper.createObjectNode();
        if (StringUtils.hasText(rawContextData)) {
            try {
                JsonNode rawNode = objectMapper.readTree(rawContextData);
                // 只处理 JSON 对象，数组/字符串等非对象类型直接丢弃
                if (rawNode instanceof ObjectNode rawObjectNode) {
                    mergedNode = rawObjectNode.deepCopy();  // deepCopy 避免修改原始对象
                }
            } catch (Exception ignored) {
                // 降级：非法JSON，丢弃原始数据，从空对象开始合并
                mergedNode = objectMapper.createObjectNode();
            }
        }

        mergedNode.put("currentScenicId", scenicId);
        if (scenicContextNode != null) {
            mergedNode.set("currentScenic", scenicContextNode);  // 景点详情存在就写入
        } else {
            // 景点查询失败，清除原始上下文中可能残留的旧景点详情，避免 LLM 使用过期数据
            mergedNode.remove("currentScenic");
        }

        // 第三段：最终序列化，失败时最小兜底只保留 scenicId。
        try {
            return objectMapper.writeValueAsString(mergedNode);
        } catch (Exception ignored) {
            // 最终兜底，至少保证 scenicId 传给 LLM
            return "{\"currentScenicId\":" + scenicId + "}";
        }
    }

    /**
     * 把上下文 JSON 转成 LLM 能理解的系统提示文本
     *
     * @param contextData 上下文数据，可能包含用户的兴趣标签、历史浏览记录等信息，如果为空则使用默认提示
     * @return 系统提示文本，如果 contextData 为空则返回默认提示 "你是旅行助手，请基于用户问题给出实用、简洁、可执行的建议。"，否则返回 "你是旅行助手，请结合以下上下文回答用户：" + contextData
     */
    public String buildSystemContextPrompt(String contextData) {
        if (!StringUtils.hasText(contextData)) {
            return "你是旅行助手，请基于用户问题给出实用、简洁、可执行的建议。";
        }
        return "你是旅行助手，请结合以下上下文回答用户：" + contextData;
    }

    /**
     * 构建景点上下文信息，查询景点的详细信息和关联标签，并将这些信息封装成 JSON 对象返回，便于在上下文中提供丰富的景点信息，帮助 LLM 理解当前景点的特点和用户可能的兴趣点。
     *
     * @param scenicId 景点ID，由前端传入，表示要查询哪个景点的详细信息和关联标签。
     * @return 包含景点详细信息和关联标签的 JSON 对象，如果景点不存在或无法获取详细信息，则返回一个标记 exists=false 的 JSON 对象，便于 LLM 理解当前景点不可用。
     */
    private ObjectNode buildScenicContextNode(Long scenicId) {
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(scenicId);
        ObjectNode scenicNode = objectMapper.createObjectNode();
        scenicNode.put("id", scenicId);

        // 景点不存在，标记 exists=false，让 LLM 知道景点无效
        if (scenicSpot == null) {
            scenicNode.put("exists", false);
            return scenicNode;
        }

        // 景点存在，写入关键字段
        scenicNode.put("exists", true);
        scenicNode.put("name", nvl(scenicSpot.getName()));
        scenicNode.put("address", nvl(scenicSpot.getAddress()));
        scenicNode.put("category", nvl(scenicSpot.getCategory()));
        scenicNode.put("level", nvl(scenicSpot.getLevel()));
        scenicNode.put("bestSeason", nvl(scenicSpot.getBestSeason()));
        scenicNode.put("openTime", nvl(scenicSpot.getOpenTime()));
        scenicNode.put("ticketInfo", nvl(scenicSpot.getTicketInfo()));
        scenicNode.put("description", abbreviate(scenicSpot.getDescription(), 200));  // 限长避免token浪费
        scenicNode.put("tips", abbreviate(scenicSpot.getTips(), 120));

        // 通过关联查询一次拿到标签名，减少数据库往返。
        List<String> tagNames = scenicSpotTagMapper.selectTagNamesByScenicSpotId(scenicId);
        scenicNode.put("tags", String.join(",", tagNames));
        return scenicNode;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    /**
     * 对文本进行简化处理，如果文本长度超过指定的最大长度，则截取前面部分并添加省略号，便于在上下文中提供简洁的信息，避免过长的文本导致 LLM 理解困难或上下文过载。
     *
     * @param value     需要简化的文本，如果为 null 则返回空字符串。
     * @param maxLength 最大长度，如果文本长度超过该值，则进行截取处理。
     * @return 简化后的文本，如果原文本长度不超过 maxLength 则返回原文本，否则返回截取后的文本加上省略号。
     */
    private String abbreviate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
