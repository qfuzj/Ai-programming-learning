package com.travel.advisor.dto.plan;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 旅行计划AI生成的景点候选项数据结构，包含景点的基本信息和排名分数等特征，供LLM生成行程草稿时参考
  * 1. scenicId、name、category、level、score 等字段描述了景点的基本信息和特征，帮助LLM理解每个候选景点的特点
  * 2. address、openTime、ticketPrice、suggestedHours 等字段提供了更多维度的信息供LLM生成更个性化的行程安排
  * 3. 通过这个类可以将输入的候选景点列表转换为一个结构化的对象列表，便于构建用户提示并指导LLM生成针对每个候选景点的行程安排
  * 4. 在构建用户提示时，可以将这个候选项列表以JSON格式传递给LLM，帮助LLM理解每个候选景点的详细信息，从而生成更有针对性的行程安排
  * 5. 这个类的设计可以根据实际需要进行扩展，比如添加更多的景点特征字段，或者添加一些辅助方法来格式化候选项信息
 */
@Data
@Builder
public class TravelPlanAiScenicCandidate {

    private Long scenicId;

    private String name;

    private String category;

    private String level;

    private Double score;

    private String address;

    private String openTime;

    private BigDecimal ticketPrice;

    private Integer suggestedHours;
}
