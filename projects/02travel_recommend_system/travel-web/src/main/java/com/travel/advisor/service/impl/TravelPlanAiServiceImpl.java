package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.common.enums.LLMCallLogStatus;
import com.travel.advisor.common.enums.TravelPlanItemType;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.llm.LlmRequest;
import com.travel.advisor.dto.llm.LlmResponse;
import com.travel.advisor.dto.plan.TravelPlanAiDraftPayload;
import com.travel.advisor.dto.plan.TravelPlanAiGenerateDTO;
import com.travel.advisor.dto.plan.TravelPlanAiScenicCandidate;
import com.travel.advisor.entity.Region;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.llm.LlmGateway;
import com.travel.advisor.llm.LlmProperties;
import com.travel.advisor.mapper.RegionMapper;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.service.LlmCallLogService;
import com.travel.advisor.service.TravelPlanAiService;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.plan.TravelPlanDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelPlanAiServiceImpl implements TravelPlanAiService {

    private static final String CALL_TYPE = "itinerary_generate";
    /** 行程生成支持的最大天数，需与 TravelPlanAiGenerateDTO.days 的 @Max 保持一致 */
    private static final int MAX_ITINERARY_DAYS = 15;
    private static final String SYSTEM_PROMPT = """
            你是旅游行程规划助手。
            你必须只输出 JSON，不要输出 Markdown、解释或额外文本。
            请根据用户的目的地、天数、预算、同行人和偏好，生成一个可执行的中文行程草案。
            返回格式必须是：
            {
              "title": "行程标题",
              "description": "整体描述",
              "days": [
                {
                  "dayNo": 1,
                  "items": [
                    {
                      "itemType": 1,
                      "scenicSpotId": 123,
                      "title": "行程项标题",
                      "description": "简短说明",
                      "startTime": "09:00",
                      "endTime": "11:00",
                      "location": "地点",
                      "estimatedCost": 80,
                      "notes": "注意事项"
                    }
                  ]
                }
              ]
            }
            约束：
            1. 必须严格输出 JSON。
            2. dayNo 必须从 1 开始，连续到用户要求的天数。
            3. itemType 只能使用 1-景点 2-餐饮 3-住宿 4-交通 5-自定义。
            4. 时间格式必须是 HH:mm。
            5. 如果使用候选景点，scenicSpotId 必须来自给定候选列表；非景点项可为 null。
            6. 每天给出 3 到 5 个行程项，整体安排要紧凑但合理。
            """;

    private final ScenicSpotMapper scenicSpotMapper;
    private final RegionMapper regionMapper;
    private final LlmGateway llmGateway;
    private final LlmProperties llmProperties;
    private final LlmCallLogService llmCallLogService;

    /**
     * 生成旅行计划草稿
     * 1. 接收用户输入的旅行计划生成请求，包含目的地、天数、预算、同行人类型、旅行风格和偏好标签等信息
     * 2. 根据目的地信息查询相关的景点候选项，构建供LLM生成行程草稿的用户提示，整合用户输入和景点候选项形成清晰的指令
     * 3. 调用LLM生成行程草稿，并解析LLM返回的JSON结果，提取行程标题、描述和每天的行程安排等信息，形成结构化的行程草稿对象
     * 4. 记录LLM调用日志，保存请求和响应内容，以及调用结果状态，便于后续分析和优化行程生成的效果
     */
    @Override
    public TravelPlanDetailVO generateDraft(TravelPlanAiGenerateDTO dto) {
        Long userId = getCurrentUserIdRequired();
        normalizeDateRange(dto);
        Region destinationRegion = resolveDestinationRegion(dto.getDestination());
        List<ScenicSpot> scenicCandidates = queryScenicCandidates(dto, destinationRegion);
        List<TravelPlanAiScenicCandidate> promptCandidates = scenicCandidates.stream()
                .map(this::toPromptCandidate)
                .toList();

        long start = System.currentTimeMillis();
        String userPrompt = buildUserPrompt(dto, destinationRegion, promptCandidates);
        LlmRequest request = LlmRequest.builder()
                .userId(userId)
                .modelName(llmProperties.getModelName())
                .jsonMode(true)
                .timeoutMs(resolveItineraryTimeoutMs())
                .messages(List.of(
                        LlmRequest.Message.builder().role("system").content(SYSTEM_PROMPT).build(),
                        LlmRequest.Message.builder().role("user").content(userPrompt).build()))
                .build();

        String requestMessages = JsonUtils.toJson(request.getMessages());
        try {
            LlmResponse response = llmGateway.generate(request);
            TravelPlanAiDraftPayload payload = parsePayload(response.getContent());
            if (payload == null || payload.getDays() == null || payload.getDays().isEmpty()) {
                llmCallLogService.saveCallLog(
                        userId,
                        CALL_TYPE,
                        userPrompt,
                        requestMessages,
                        response,
                        LLMCallLogStatus.FAILED.getCode(),
                        "LLM 返回的行程草案 JSON 无法解析",
                        (int) (System.currentTimeMillis() - start));
                return buildFallbackDraft(dto, destinationRegion, scenicCandidates);
            }
            llmCallLogService.saveCallLog(
                    userId,
                    CALL_TYPE,
                    userPrompt,
                    requestMessages,
                    response,
                    LLMCallLogStatus.SUCCESS.getCode(),
                    null,
                    (int) (System.currentTimeMillis() - start));
            return toDraftVO(dto, destinationRegion, payload, scenicCandidates);
        } catch (Exception ex) {
            llmCallLogService.saveCallLog(
                    userId,
                    CALL_TYPE,
                    userPrompt,
                    requestMessages,
                    null,
                    resolveStatus(ex).getCode(),
                    ex.getMessage(),
                    (int) (System.currentTimeMillis() - start));
            return buildFallbackDraft(dto, destinationRegion, scenicCandidates);
        }
    }

    /**
     * 解析目的地信息，优先进行精确匹配，如果没有找到则进行模糊匹配，最终返回匹配到的 Region 对象或者 null
     * 1. 首先检查输入的目的地字符串是否为空或仅包含空白字符，如果是则直接返回 null，表示没有有效的目的地信息
     * 2. 使用 regionMapper 进行数据库查询，首先尝试精确匹配目的地名称，按照行政级别升序排序并限制结果为1条，如果找到了匹配的 Region
     * 则返回该对象
     * 3. 如果没有找到精确匹配的 Region，则进行模糊匹配，使用 like 查询目的地名称，按照行政级别升序排序并限制结果为1条，如果找到了匹配的
     * Region 则返回该对象
     * 4. 如果两次查询都没有找到匹配的 Region，则返回 null，表示无法解析出有效的目的地区域信息
     */
    private Region resolveDestinationRegion(String destination) {
        if (destination == null || destination.isBlank()) {
            return null;
        }
        List<Region> exact = regionMapper.selectList(new LambdaQueryWrapper<Region>()
                .eq(Region::getName, destination.trim())
                .orderByAsc(Region::getLevel)
                .last("limit 1"));
        if (!exact.isEmpty()) {
            return exact.get(0);
        }
        List<Region> fuzzy = regionMapper.selectList(new LambdaQueryWrapper<Region>()
                .like(Region::getName, destination.trim())
                .orderByAsc(Region::getLevel)
                .last("limit 1"));
        return fuzzy.isEmpty() ? null : fuzzy.get(0);
    }

    /**
     * 查询景点候选项，根据目的地区域信息优先进行区域匹配，如果没有找到则进行名称模糊匹配，最终返回一个景点列表供LLM生成行程草稿时参考
     * 1. 根据输入的 DTO 中的目的地信息解析出对应的 Region 对象，如果解析成功则使用该 Region 的 ID
     * 进行景点查询，否则使用目的地字符串进行模糊匹配查询
     * 2. 查询条件包括景点状态为正常，并按照评分、收藏数量和浏览数量等维度进行降序排序，限制返回的结果数量在一定范围内（根据天数动态调整）
     * 3. 如果根据区域信息查询没有找到任何景点，并且之前是使用区域查询的方式，则退回到使用名称模糊匹配的方式进行查询，以增加找到相关景点的可能性
     * 4. 最终返回查询到的景点列表，如果两次查询都没有找到任何景点，则返回一个空列表，表示没有可用的景点候选项供LLM参考
     *
     */
    private List<ScenicSpot> queryScenicCandidates(TravelPlanAiGenerateDTO dto, Region destinationRegion) {
        int limit = Math.min(Math.max(dto.getDays() * 4, 6), 15);
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<ScenicSpot>()
                .eq(ScenicSpot::getStatus, 1)
                .orderByDesc(ScenicSpot::getScore)
                .orderByDesc(ScenicSpot::getFavoriteCount)
                .orderByDesc(ScenicSpot::getViewCount)
                .last("limit " + limit);
        if (destinationRegion != null) {
            wrapper.eq(ScenicSpot::getRegionId, destinationRegion.getId());
        } else {
            wrapper.and(w -> w.like(ScenicSpot::getName, dto.getDestination())
                    .or()
                    .like(ScenicSpot::getAddress, dto.getDestination()));
        }
        // 查询命中目的地的候选景点；若目的地存在但景点库为空，宁可返回空列表也不降级为
        // 全站 Top-N，避免把无关城市的景点喂给 LLM 造成误导
        return scenicSpotMapper.selectList(wrapper);
    }

    /**
     * 将 ScenicSpot 对象转换为 TravelPlanAiScenicCandidate 对象，提取景点的基本信息和特征，供LLM生成行程草稿时参考
     * 1. scenicId、name、category、level、score 等字段描述了景点的基本信息和特征，帮助LLM理解每个候选景点的特点
     * 2. address、openTime、ticketPrice、suggestedHours 等字段提供了更多维度的信息供LLM生成更个性化的行程安排
     * 3. 通过这个方法可以将数据库查询到的 ScenicSpot 对象转换为一个结构化的
     * TravelPlanAiScenicCandidate对象，便于构建用户提示并指导LLM生成针对每个候选景点的行程安排
     * 4. 在构建用户提示时，可以将这个候选项列表以JSON格式传递给LLM，帮助LLM理解每个候选景点的详细信息，从而生成更有针对性的行程安排
     * 5. 这个方法的设计可以根据实际需要进行扩展，比如添加更多的景点特征字段，或者添加一些辅助方法来格式化候选项信息
     */
    private TravelPlanAiScenicCandidate toPromptCandidate(ScenicSpot scenicSpot) {
        return TravelPlanAiScenicCandidate.builder()
                .scenicId(scenicSpot.getId())
                .name(scenicSpot.getName())
                .category(scenicSpot.getCategory())
                .level(scenicSpot.getLevel())
                .score(scenicSpot.getScore())
                .address(scenicSpot.getAddress())
                .openTime(scenicSpot.getOpenTime())
                .ticketPrice(scenicSpot.getTicketPrice())
                .suggestedHours(scenicSpot.getSuggestedHours())
                .build();
    }

    /**
     * 构建用户提示，整合用户输入的旅行计划生成参数和景点候选项信息，形成一个清晰的指令供LLM生成行程草稿时参考
     */
    private String buildUserPrompt(TravelPlanAiGenerateDTO dto,
            Region destinationRegion,
            List<TravelPlanAiScenicCandidate> promptCandidates) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("destination", dto.getDestination());
        payload.put("destinationRegionId", destinationRegion == null ? null : destinationRegion.getId());
        payload.put("destinationRegionName", destinationRegion == null ? null : destinationRegion.getName());
        payload.put("days", dto.getDays());
        payload.put("startDate", dto.getStartDate());
        payload.put("endDate", dto.getEndDate());
        payload.put("budget", dto.getBudget());
        payload.put("companionType", dto.getCompanionType());
        payload.put("travelStyle", dto.getTravelStyle());
        payload.put("preferredTags", dto.getPreferredTags());
        payload.put("candidateScenicSpots", promptCandidates);
        return """
                请生成一份旅游行程草案，并以 JSON 返回。
                用户输入：
                %s
                """.formatted(JsonUtils.toJson(payload));
    }

    /**
     * 解析LLM返回的行程草案内容
     * 1. 验证输入内容是否为空或仅包含空白字符，如果是则返回 null，表示无法解析出有效的行程草案信息
     * 2. 对输入内容进行预处理，去除可能存在的 Markdown 代码块标记（```json ... ```），以提取出纯净的 JSON 字符串
     * 3. 使用 JsonUtils 将预处理后的字符串解析为 TravelPlanAiDraftPayload 对象，如果解析过程中发生任何异常（如 JSON
     * 格式错误），则捕获异常并返回 null，表示解析失败
     * 4. 如果解析成功，则返回解析得到的 TravelPlanAiDraftPayload 对象，供后续构建行程草稿VO使用
     */
    private TravelPlanAiDraftPayload parsePayload(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String normalized = content.trim();
        // 1) 去除 Markdown 代码围栏（```json ... ``` / ``` ... ```）
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```(?:json)?\\s*", "");
            normalized = normalized.replaceFirst("```\\s*$", "");
            normalized = normalized.trim();
        }
        // 2) 兜底切片：若 LLM 在 JSON 前后夹带自然语言，则取第一个 '{' 到最后一个 '}' 的子串
        if (!normalized.startsWith("{")) {
            int left = normalized.indexOf('{');
            int right = normalized.lastIndexOf('}');
            if (left >= 0 && right > left) {
                normalized = normalized.substring(left, right + 1);
            }
        }
        try {
            return JsonUtils.fromJson(normalized, TravelPlanAiDraftPayload.class);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 将LLM返回的行程草案内容转换为行程草稿VO
     * 1. 从输入的 DTO、目的地区域信息、LLM解析得到的行程草案数据结构以及景点候选项列表中提取必要的信息，构建一个完整的
     * TravelPlanDetailVO 对象
     * 2. 行程草稿VO包含行程的标题、描述、每天的行程安排等信息，供前端展示和后续编辑使用
     */
    private TravelPlanDetailVO toDraftVO(TravelPlanAiGenerateDTO dto,
            Region destinationRegion,
            TravelPlanAiDraftPayload payload,
            List<ScenicSpot> scenicCandidates) {
        TravelPlanDetailVO vo = buildBaseDraft(dto, destinationRegion, scenicCandidates);
        vo.setTitle(isBlank(payload.getTitle()) ? buildDefaultTitle(dto) : payload.getTitle().trim());
        vo.setDescription(
                isBlank(payload.getDescription()) ? buildDefaultDescription(dto) : payload.getDescription().trim());
        vo.setDays(toDayVOs(dto.getDays(), payload.getDays(), scenicCandidates));
        return vo;
    }

    private TravelPlanDetailVO buildFallbackDraft(TravelPlanAiGenerateDTO dto,
            Region destinationRegion,
            List<ScenicSpot> scenicCandidates) {
        TravelPlanDetailVO vo = buildBaseDraft(dto, destinationRegion, scenicCandidates);
        vo.setTitle(buildDefaultTitle(dto));
        vo.setDescription(buildDefaultDescription(dto));

        List<TravelPlanDetailVO.TravelPlanDayVO> days = new ArrayList<>();
        int scenicIndex = 0;
        for (int dayNo = 1; dayNo <= dto.getDays(); dayNo++) {
            TravelPlanDetailVO.TravelPlanDayVO dayVO = new TravelPlanDetailVO.TravelPlanDayVO();
            dayVO.setDayNo(dayNo);
            List<TravelPlanDetailVO.TravelPlanItemVO> items = new ArrayList<>();

            items.add(buildItem(dayNo, 1, TravelPlanItemType.TRANSPORT.getCode(),
                    "前往今日行程区域", "根据出发位置灵活安排交通方式", "09:00", "09:40",
                    dto.getDestination(), BigDecimal.ZERO, "建议提前查看实时交通"));

            ScenicSpot scenicSpot = scenicCandidates.isEmpty() ? null
                    : scenicCandidates.get(scenicIndex % scenicCandidates.size());
            scenicIndex++;
            items.add(buildItem(dayNo, 2, TravelPlanItemType.SCENIC.getCode(),
                    scenicSpot == null ? dto.getDestination() + " 核心景点游览" : scenicSpot.getName(),
                    scenicSpot == null ? "优先安排目的地代表性景点" : defaultIfBlank(scenicSpot.getDescription(), "安排目的地代表性景点游览"),
                    "10:00", "12:00",
                    scenicSpot == null ? dto.getDestination() : scenicSpot.getAddress(),
                    scenicSpot == null ? BigDecimal.ZERO : defaultIfNull(scenicSpot.getTicketPrice(), BigDecimal.ZERO),
                    scenicSpot == null ? "可根据天气调整顺序" : defaultIfBlank(scenicSpot.getTips(), "建议错峰前往"),
                    scenicSpot == null ? null : scenicSpot.getId()));

            items.add(buildItem(dayNo, 3, TravelPlanItemType.FOOD.getCode(),
                    "午餐与短暂休息", "在景点附近安排一顿有当地特色的用餐", "12:20", "13:20",
                    dto.getDestination(), estimateMealCost(dto), "可根据口味临时调整"));

            items.add(buildItem(dayNo, 4, TravelPlanItemType.CUSTOM.getCode(),
                    "自由活动 / 夜游", "留出弹性时间给拍照、逛街或夜景体验", "15:00", "18:00",
                    dto.getDestination(), BigDecimal.ZERO, "根据体力和天气灵活调整"));

            dayVO.setItems(items);
            days.add(dayVO);
        }
        vo.setDays(days);
        return vo;
    }

    /**
     * 构建行程草稿VO的基础信息部分
     * 包含行程的标题、封面图片、总天数、日期范围、目的地区域ID、描述、预算、同行人类型、公开状态、来源和状态等基本属性，为后续填充每天的行程安排做好准备
     */
    private TravelPlanDetailVO buildBaseDraft(TravelPlanAiGenerateDTO dto,
            Region destinationRegion,
            List<ScenicSpot> scenicCandidates) {
        TravelPlanDetailVO vo = new TravelPlanDetailVO();
        vo.setTitle(buildDefaultTitle(dto));
        vo.setCoverImage(scenicCandidates.isEmpty() ? null : scenicCandidates.get(0).getCoverImage());
        vo.setTotalDays(dto.getDays());
        vo.setStartDate(dto.getStartDate());
        vo.setEndDate(dto.getEndDate());
        vo.setDestinationRegionId(destinationRegion == null ? null : destinationRegion.getId());
        vo.setDescription(buildDefaultDescription(dto));
        vo.setEstimatedBudget(dto.getBudget());
        vo.setTravelCompanion(dto.getCompanionType());
        vo.setIsPublic(0);
        vo.setSource(2);
        vo.setStatus(0);
        return vo;
    }

    /**
     * 将LLM返回的每天的行程安排内容转换为行程草稿VO中的每天行程安排部分
     * 1. 根据输入的总天数、LLM解析得到的每天行程安排数据结构以及景点候选项列表，构建一个每天的行程安排列表
     * 2. 每天的行程安排包含当天的行程项列表，每个行程项包含类型、标题、描述、时间、地点、预计费用等信息，供前端展示和后续编辑使用
     */
    private List<TravelPlanDetailVO.TravelPlanDayVO> toDayVOs(Integer totalDays,
            List<TravelPlanAiDraftPayload.DayPayload> payloadDays,
            List<ScenicSpot> scenicCandidates) {
        Map<Long, ScenicSpot> scenicMap = scenicCandidates.stream()
                .collect(Collectors.toMap(ScenicSpot::getId, item -> item, (left, right) -> left,
                        LinkedHashMap::new));
        List<TravelPlanDetailVO.TravelPlanDayVO> days = new ArrayList<>();
        for (int dayNo = 1; dayNo <= totalDays; dayNo++) {
            int currentDayNo = dayNo;
            TravelPlanDetailVO.TravelPlanDayVO dayVO = new TravelPlanDetailVO.TravelPlanDayVO();
            dayVO.setDayNo(currentDayNo);
            TravelPlanAiDraftPayload.DayPayload payloadDay = payloadDays.stream()
                    .filter(item -> item != null && currentDayNo == defaultIfNull(item.getDayNo(), 0))
                    .findFirst()
                    .orElse(null);
            if (payloadDay == null || payloadDay.getItems() == null || payloadDay.getItems().isEmpty()) {
                dayVO.setItems(Collections.emptyList());
            } else {
                List<TravelPlanDetailVO.TravelPlanItemVO> items = new ArrayList<>();
                for (int i = 0; i < payloadDay.getItems().size(); i++) {
                    TravelPlanAiDraftPayload.ItemPayload itemPayload = payloadDay.getItems().get(i);
                    items.add(toItemVO(currentDayNo, i + 1, itemPayload, scenicMap));
                }
                dayVO.setItems(items);
            }
            days.add(dayVO);
        }
        return days;
    }

    /**
     * 将LLM返回的每天的行程安排中的每个行程项内容转换为行程草稿VO中的行程项部分
     */
    private TravelPlanDetailVO.TravelPlanItemVO toItemVO(int dayNo,
            int sortOrder,
            TravelPlanAiDraftPayload.ItemPayload itemPayload,
            Map<Long, ScenicSpot> scenicMap) {
        TravelPlanDetailVO.TravelPlanItemVO itemVO = new TravelPlanDetailVO.TravelPlanItemVO();
        itemVO.setDayNo(dayNo);
        itemVO.setSortOrder(sortOrder);
        itemVO.setItemType(resolveItemType(itemPayload == null ? null : itemPayload.getItemType()));
        itemVO.setScenicSpotId(itemPayload == null ? null : itemPayload.getScenicSpotId());
        ScenicSpot scenicSpot = itemPayload == null ? null : scenicMap.get(itemPayload.getScenicSpotId());
        itemVO.setTitle(defaultIfBlank(itemPayload == null ? null : itemPayload.getTitle(),
                scenicSpot == null ? "未命名行程项" : scenicSpot.getName()));
        itemVO.setDescription(defaultIfBlank(itemPayload == null ? null : itemPayload.getDescription(),
                scenicSpot == null ? "" : defaultIfBlank(scenicSpot.getDescription(), "")));
        itemVO.setStartTime(parseTime(itemPayload == null ? null : itemPayload.getStartTime()));
        itemVO.setEndTime(parseTime(itemPayload == null ? null : itemPayload.getEndTime()));
        itemVO.setLocation(defaultIfBlank(itemPayload == null ? null : itemPayload.getLocation(),
                scenicSpot == null ? "" : scenicSpot.getAddress()));
        itemVO.setEstimatedCost(itemPayload == null ? null : itemPayload.getEstimatedCost());
        itemVO.setNotes(itemPayload == null ? null : itemPayload.getNotes());
        itemVO.setLongitude(scenicSpot == null ? null : scenicSpot.getLongitude());
        itemVO.setLatitude(scenicSpot == null ? null : scenicSpot.getLatitude());
        return itemVO;
    }

    private TravelPlanDetailVO.TravelPlanItemVO buildItem(int dayNo,
            int sortOrder,
            int itemType,
            String title,
            String description,
            String startTime,
            String endTime,
            String location,
            BigDecimal estimatedCost,
            String notes) {
        return buildItem(dayNo, sortOrder, itemType, title, description, startTime, endTime, location, estimatedCost,
                notes, null);
    }

    private TravelPlanDetailVO.TravelPlanItemVO buildItem(int dayNo,
            int sortOrder,
            int itemType,
            String title,
            String description,
            String startTime,
            String endTime,
            String location,
            BigDecimal estimatedCost,
            String notes,
            Long scenicSpotId) {
        TravelPlanDetailVO.TravelPlanItemVO itemVO = new TravelPlanDetailVO.TravelPlanItemVO();
        itemVO.setDayNo(dayNo);
        itemVO.setSortOrder(sortOrder);
        itemVO.setItemType(itemType);
        itemVO.setTitle(title);
        itemVO.setDescription(description);
        itemVO.setStartTime(parseTime(startTime));
        itemVO.setEndTime(parseTime(endTime));
        itemVO.setLocation(location);
        itemVO.setEstimatedCost(estimatedCost);
        itemVO.setNotes(notes);
        itemVO.setScenicSpotId(scenicSpotId);
        return itemVO;
    }

    private Integer resolveItemType(Integer itemType) {
        TravelPlanItemType type = TravelPlanItemType.fromCode(itemType);
        return type == null ? TravelPlanItemType.CUSTOM.getCode() : type.getCode();
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal estimateMealCost(TravelPlanAiGenerateDTO dto) {
        if (dto.getBudget() == null || dto.getDays() == null || dto.getDays() <= 0) {
            return BigDecimal.valueOf(80);
        }
        return dto.getBudget()
                .divide(BigDecimal.valueOf(dto.getDays()), 0, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(0.2))
                .setScale(0, java.math.RoundingMode.HALF_UP);
    }

    private String buildDefaultTitle(TravelPlanAiGenerateDTO dto) {
        return dto.getDestination().trim() + dto.getDays() + "天行程草案";
    }

    /**
     * 构建默认行程描述，根据用户输入的旅行计划生成参数整合形成一个简洁的描述文本，供行程草稿使用
     */
    private String buildDefaultDescription(TravelPlanAiGenerateDTO dto) {
        List<String> parts = new ArrayList<>();
        parts.add("围绕 " + dto.getDestination().trim() + " 安排 " + dto.getDays() + " 天行程");
        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            parts.add("出行日期为 " + dto.getStartDate() + " 至 " + dto.getEndDate());
        }
        if (dto.getTravelStyle() != null && !dto.getTravelStyle().isBlank()) {
            parts.add("风格偏向 " + dto.getTravelStyle());
        }
        if (dto.getCompanionType() != null && !dto.getCompanionType().isBlank()) {
            parts.add("适合同伴类型 " + dto.getCompanionType());
        }
        if (dto.getPreferredTags() != null && !dto.getPreferredTags().isEmpty()) {
            parts.add("重点偏好 " + String.join("、", dto.getPreferredTags()));
        }
        return String.join("，", parts) + "。";
    }

    private LLMCallLogStatus resolveStatus(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains("timeout")) {
                return LLMCallLogStatus.TIMEOUT;
            }
            current = current.getCause();
        }
        return LLMCallLogStatus.FAILED;
    }

    /**
     * 规范化日期范围，计算实际天数并设置到 DTO 中
     */
    private void normalizeDateRange(TravelPlanAiGenerateDTO dto) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        if (startDate == null || endDate == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "出发日期和结束日期不能为空");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结束日期不能早于出发日期");
        }
        int actualDays = Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate) + 1);
        // 该校验在 @Valid 之后执行，必须二次把关：@Max(15) 仅校验入参 days，
        // 但实际生成按日期差重算，用户完全可用日期区间绕过 days 上限
        if (actualDays > MAX_ITINERARY_DAYS) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "行程天数不能超过 " + MAX_ITINERARY_DAYS + " 天");
        }
        dto.setDays(actualDays);
    }

    private Integer resolveItineraryTimeoutMs() {
        Integer timeoutMs = llmProperties.getItineraryTimeoutMs();
        if (timeoutMs == null || timeoutMs <= 0) {
            return llmProperties.getTimeoutMs();
        }
        return timeoutMs;
    }

    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private <T> T defaultIfNull(T value, T fallback) {
        return value == null ? fallback : value;
    }
}
