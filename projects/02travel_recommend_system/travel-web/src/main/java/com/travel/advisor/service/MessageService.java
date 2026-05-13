package com.travel.advisor.service;

import com.travel.advisor.dto.chat.ChatSendMessageDTO;
import com.travel.advisor.vo.chat.ChatMessageVO;
import com.travel.advisor.vo.chat.ChatSendMessageVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface MessageService {

    /** 查询会话消息列表 */
    List<ChatMessageVO> listMessages(Long conversationId);

    /** 发送消息并获取 LLM 回复 */
    ChatSendMessageVO sendMessage(Long conversationId, ChatSendMessageDTO dto);

    /** 流式发送消息并获取 LLM 回复 */
    Flux<String> sendMessageStream(Long conversationId, ChatSendMessageDTO dto);
}
