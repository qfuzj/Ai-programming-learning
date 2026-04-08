package com.travel.advisor.service;

import com.travel.advisor.dto.chat.ChatConversationCreateDTO;
import com.travel.advisor.entity.LlmConversation;
import com.travel.advisor.vo.chat.ChatConversationVO;

import java.util.List;

public interface ConversationService {

    Long createConversation(ChatConversationCreateDTO dto);

    List<ChatConversationVO> listMyConversations();

    ChatConversationVO getConversation(Long conversationId);

    void deleteConversation(Long conversationId);

    LlmConversation getConversationEntity(Long conversationId, Long userId);
}
