package com.travel.advisor.controller.common;

import com.travel.advisor.common.enums.*;
import com.travel.advisor.common.enums.ScenicCategory;
import com.travel.advisor.common.enums.ScenicLevel;
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

    /**
     * 出游同伴类型字典（AI 行程生成表单）
     */
    @GetMapping("/travel-companion-type")
    public Result<List<DictVO>> getTravelCompanionTypes() {
        List<DictVO> list = Arrays.stream(TravelCompanionType.values())
                .map(type -> new DictVO(type.getCode(), type.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 旅行风格字典（AI 行程生成表单）
     */
    @GetMapping("/travel-style")
    public Result<List<DictVO>> getTravelStyles() {
        List<DictVO> list = Arrays.stream(TravelStyle.values())
                .map(style -> new DictVO(style.getCode(), style.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 公开状态字典（行程 / 景点可见性）
     */
    @GetMapping("/public-status")
    public Result<List<DictVO>> getPublicStatuses() {
        List<DictVO> list = Arrays.stream(PublicStatus.values())
                .map(status -> new DictVO(status.getCode(), status.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 系统配置分组字典（admin 系统配置页）
     */
    @GetMapping("/config-group")
    public Result<List<DictVO>> getConfigGroups() {
        List<DictVO> list = Arrays.stream(ConfigGroup.values())
                .map(group -> new DictVO(group.getCode(), group.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 系统配置值类型字典（admin 系统配置页）
     */
    @GetMapping("/config-type")
    public Result<List<DictVO>> getConfigTypes() {
        List<DictVO> list = Arrays.stream(ConfigType.values())
                .map(type -> new DictVO(type.getCode(), type.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 操作日志模块字典（admin 操作日志筛选）
     */
    @GetMapping("/operation-log-module")
    public Result<List<DictVO>> getOperationLogModules() {
        List<DictVO> list = Arrays.stream(OperationLogModule.values())
                .map(module -> new DictVO(module.getCode(), module.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 操作日志状态字典（admin 操作日志筛选）
     */
    @GetMapping("/operation-log-status")
    public Result<List<DictVO>> getOperationLogStatuses() {
        List<DictVO> list = Arrays.stream(OperationLogStatus.values())
                .map(status -> new DictVO(status.getCode(), status.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 文件资源上传者类型字典（admin 文件管理）
     */
    @GetMapping("/file-resource-uploader-type")
    public Result<List<DictVO>> getFileResourceUploaderTypes() {
        List<DictVO> list = Arrays.stream(FileResourceUploaderType.values())
                .map(type -> new DictVO(type.getCode(), type.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 性别字典
     */
    @GetMapping("/gender")
    public Result<List<DictVO>> getGenders() {
        List<DictVO> list = Arrays.stream(Gender.values())
                .map(g -> new DictVO(g.getCode(), g.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 通用启停状态字典（用户 / 标签等）
     */
    @GetMapping("/common-status")
    public Result<List<DictVO>> getCommonStatuses() {
        List<DictVO> list = Arrays.stream(CommonStatus.values())
                .map(s -> new DictVO(s.getCode(), s.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 标签作用域字典
     */
    @GetMapping("/tag-scope")
    public Result<List<DictVO>> getTagScopes() {
        List<DictVO> list = Arrays.stream(TagScope.values())
                .map(s -> new DictVO(s.getCode(), s.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 行政区划层级字典
     */
    @GetMapping("/region-level")
    public Result<List<DictVO>> getRegionLevels() {
        List<DictVO> list = Arrays.stream(RegionLevel.values())
                .map(l -> new DictVO(l.getCode(), l.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 行程计划发布状态字典
     */
    @GetMapping("/travel-plan-status")
    public Result<List<DictVO>> getTravelPlanStatuses() {
        List<DictVO> list = Arrays.stream(TravelPlanStatus.values())
                .map(s -> new DictVO(s.getCode(), s.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 通用是否标志字典（region.is_hot 等）
     */
    @GetMapping("/yes-no-flag")
    public Result<List<DictVO>> getYesNoFlags() {
        List<DictVO> list = Arrays.stream(YesNoFlag.values())
                .map(f -> new DictVO(f.getCode(), f.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 景点等级字典
     */
    @GetMapping("/scenic-level")
    public Result<List<DictVO>> getScenicLevels() {
        List<DictVO> list = Arrays.stream(ScenicLevel.values())
                .filter(level -> level != ScenicLevel.NONE)
                .map(level -> new DictVO(level.getCode(), level.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 景点分类字典
     */
    @GetMapping("/scenic-category")
    public Result<List<DictVO>> getScenicCategories() {
        List<DictVO> list = Arrays.stream(ScenicCategory.values())
                .map(category -> new DictVO(category.getCode(), category.getDesc()))
                .collect(Collectors.toList());
        return Result.success(list);
    }
}
