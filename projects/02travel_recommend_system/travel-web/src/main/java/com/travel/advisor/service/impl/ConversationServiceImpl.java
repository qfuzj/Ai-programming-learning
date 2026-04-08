package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.common.enums.ConversationStatus;
import com.travel.advisor.common.enums.ConversationType;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.chat.ChatConversationCreateDTO;
import com.travel.advisor.entity.LlmConversation;
import com.travel.advisor.entity.LlmMessage;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.LlmConversationMapper;
import com.travel.advisor.mapper.LlmMessageMapper;
import com.travel.advisor.service.ConversationService;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.chat.ChatConversationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final LlmConversationMapper llmConversationMapper;
    private final LlmMessageMapper llmMessageMapper;

    /**
     * 创建新的会话，初始化会话数据并保存到数据库中，返回新会话的ID。
     *
     * @param dto - 包含会话标题和类型等信息的DTO对象，由前端传入。
     * @return 新创建的会话ID，供前端后续操作使用。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConversation(ChatConversationCreateDTO dto) {
        Long userId = getCurrentUserIdRequired();
        LlmConversation conversation = new LlmConversation();
        conversation.setUserId(userId);
        conversation.setTitle(dto.getTitle());
        conversation.setConversationType(dto.getConversationType());
        conversation.setMessageCount(0);
        conversation.setTotalTokens(0);
        conversation.setStatus(ConversationStatus.ACTIVE.getCode());
        llmConversationMapper.insert(conversation);
        return conversation.getId();
    }

    /**
     * 获取当前用户的所有会话列表，按照最后消息时间和创建时间降序排序，返回会话的VO列表供前端展示。
     *
     * @return 当前用户的会话列表，每个会话包含ID、标题、类型、消息数量、总token数、最后消息时间、状态等信息。
     */
    @Override
    public List<ChatConversationVO> listMyConversations() {
        Long userId = getCurrentUserIdRequired();
        // 查询数据库中属于当前用户的会话列表，按照最后消息时间和创建时间降序排序，确保最新的会话显示在前面。
        List<LlmConversation> conversations = llmConversationMapper.selectList(new LambdaQueryWrapper<LlmConversation>()
                .eq(LlmConversation::getUserId, userId)
                .orderByDesc(LlmConversation::getLastMessageAt)
                .orderByDesc(LlmConversation::getCreateTime));
        return conversations.stream().map(this::toVO).toList();
    }

    /**
     * 根据会话ID获取会话详情，首先验证会话是否属于当前用户，如果存在则将数据库中的会话实体转换为VO对象返回给前端。
     *
     * @param conversationId - 前端传入的会话ID，用于查询对应的会话详情。
     * @return 会话详情的VO对象，包含会话ID、标题、类型、消息数量、总token数、最后消息时间、状态等信息，供前端展示。
     */
    @Override
    public ChatConversationVO getConversation(Long conversationId) {
        Long userId = getCurrentUserIdRequired();
        return toVO(getConversationEntity(conversationId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId) {
        Long userId = getCurrentUserIdRequired();
        LlmConversation conversation = getConversationEntity(conversationId, userId);
        llmConversationMapper.deleteById(conversation.getId());
        llmMessageMapper.delete(new LambdaQueryWrapper<LlmMessage>()
                .eq(LlmMessage::getConversationId, conversation.getId()));
    }

    /**
     * 根据会话ID和用户ID查询会话实体，验证会话是否存在且属于当前用户，如果验证失败则抛出异常。
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 会话实体对象，包含会话的所有字段信息，供后续操作使用。
     */
    @Override
    public LlmConversation getConversationEntity(Long conversationId, Long userId) {
        LlmConversation conversation = llmConversationMapper.selectOne(new LambdaQueryWrapper<LlmConversation>()
                .eq(LlmConversation::getId, conversationId)
                .eq(LlmConversation::getUserId, userId));
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        return conversation;
    }

    /**
     * 将 LlmConversation 实体转换为 ChatConversationVO 视图对象，便于前端展示会话信息。
     *
     * @param conversation - 数据库中的会话实体对象，包含会话的所有字段信息。
     * @return ChatConversationVO - 包含会话ID、标题、类型、消息数量、总token数、最后消息时间、状态等信息的视图对象。
     */
    private ChatConversationVO toVO(LlmConversation conversation) {
        ChatConversationVO vo = new ChatConversationVO();
        vo.setId(conversation.getId());
        vo.setTitle(conversation.getTitle());
        ConversationType type = ConversationType.fromCode(conversation.getConversationType());
        vo.setConversationType(type.getScene());
        vo.setMessageCount(conversation.getMessageCount());
        vo.setTotalTokens(conversation.getTotalTokens());
        vo.setLastMessageAt(conversation.getLastMessageAt());
        ConversationStatus status = ConversationStatus.fromCode(conversation.getStatus());
        vo.setStatus(status.getDesc());
        vo.setCreateTime(conversation.getCreateTime());
        vo.setUpdateTime(conversation.getUpdateTime());
        return vo;
    }


    private Long getCurrentUserIdRequired() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
