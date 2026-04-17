package com.travel.advisor.controller.common;

import com.travel.advisor.common.enums.*;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.DictVO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类型字典接口
 */
@RestController
@RequestMapping({"/dict", "/api/dict"})
public class DictController {

    /**
     * 用户审核状态字典
     */
    @GetMapping("/user-review-status")
    public Result<List<DictVO>> getUserReviewStatuses() {
        List<DictVO> list = Arrays.stream(UserReviewStatus.values())
                .map(status -> new DictVO(status.getCode(), status.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 敏感状态字典
     */
    @GetMapping("/sensitive-status")
    public Result<List<DictVO>> getSensitiveStatuses() {
        List<DictVO> list = Arrays.stream(SensitiveStatus.values())
                .map(status -> new DictVO(status.getCode(), status.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 推荐类型字典
     */
    @GetMapping("/recommend-type")
    public Result<List<DictVO>> getRecommendTypes() {
        List<DictVO> list = Arrays.stream(RecommendType.values())
                .map(type -> new DictVO(type.getCode(), type.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * LLM调用日志状态字典
     */
    @GetMapping("/llm-call-log-status")
    public Result<List<DictVO>> getLLMCallLogStatus() {
        List<DictVO> list = Arrays.stream(LLMCallLogStatus.values())
                .map(status -> new DictVO(status.getCode(), status.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 内容审核状态字典
     */
    @GetMapping("/content-audit-status")
    public Result<List<DictVO>> getContentAuditStatus() {
        List<DictVO> list = Arrays.stream(ContentAuditStatus.values())
                .map(status -> new DictVO(status.getCode(), status.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 会话类型字典
     */
    @GetMapping("/conversation-type")
    public Result<List<DictVO>> getConversationTypes() {
        List<DictVO> list = Arrays.stream(ConversationType.values())
                .map(type -> new DictVO(type.getCode(), type.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 会话状态字典
     */
    @GetMapping("/conversation-status")
    public Result<List<DictVO>> getConversationStatus() {
        List<DictVO> list = Arrays.stream(ConversationStatus.values())
                .map(status -> new DictVO(status.getCode(), status.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 消息内容类型字典
     */
    @GetMapping("/message-content-type")
    public Result<List<DictVO>> getMessageContentTypes() {
        List<DictVO> list = Arrays.stream(MessageContentType.values())
                .map(type -> new DictVO(type.getCode(), type.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 业务类型字典
     */
    @GetMapping("/biz-type")
    public Result<List<DictVO>> getBizTypes() {
        List<DictVO> list = Arrays.stream(BizType.values())
                .map(type -> new DictVO(type.getCode(), type.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 文件资源状态枚举
     */
    @GetMapping("/file-resource-status")
    public Result<List<DictVO>> getFileResourceStatuses() {
        List<DictVO> list = Arrays.stream(FileResourceStatus.values())
                .map(status -> new DictVO(status.getCode(), status.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 消息角色枚举
     */
    @GetMapping("/message-roles")
    public Result<List<DictVO>> getMessageRoles() {
        List<DictVO> list = Arrays.stream(MessageRole.values())
                .map(role -> new DictVO(role.getRole(), role.getRole()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 行程项类型字典
     */
    @GetMapping("/travel-plan-item-type")
    public Result<List<DictVO>> getTravelPlanItemTypes() {
        List<DictVO> list = Arrays.stream(TravelPlanItemType.values())
                .map(type -> new DictVO(type.getCode(), type.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }
}
