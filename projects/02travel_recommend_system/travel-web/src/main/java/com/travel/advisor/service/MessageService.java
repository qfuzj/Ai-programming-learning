package com.travel.advisor.service;

import com.travel.advisor.dto.chat.ChatSendMessageDTO;
import com.travel.advisor.vo.chat.ChatMessageVO;
import com.travel.advisor.vo.chat.ChatSendMessageVO;

import java.util.List;

public interface MessageService {

    /**
     * 获取指定会话的消息列表，首先验证当前用户对该会话的访问权限，然后查询数据库中属于该会话的消息记录，按照创建时间和ID升序排序，最后将消息实体转换为VO对象列表返回给前端展示。
     *
     * @param conversationId - 会话ID，由前端传入，表示要查询哪个会话的消息列表。
     * @return 指定会话的消息列表，每条消息包含ID、角色（用户或助手）、内容、内容类型、使用的token数量、关联的LLM调用日志ID、创建时间等信息。
     */
    List<ChatMessageVO> listMessages(Long conversationId);

    /**
     * 发送消息到指定会话，首先验证当前用户对该会话的访问权限，然后将用户输入的消息内容和可选的上下文信息（如景点ID）封装成LLM聊天请求对象，调用LLM网关接口获取LLM生成的回复内容，最后将用户消息和LLM回复保存到数据库中，并返回LLM回复的VO对象给前端展示。
     * @param conversationId - 会话ID，由前端传入，表示要发送消息到哪个会话。
     * @param dto - 用户发送消息的DTO对象，包含用户输入的消息内容和可选的上下文信息（如景点ID），前端需要对用户输入进行校验，确保发送的消息有效且符合预期格式，便于后端处理和生成回复。
     * @return LLM回复的VO对象，包含LLM生成的回复内容，前端可以直接展示给用户，或者根据需要进行进一步处理（如格式化、提取关键信息等）。
     */
    ChatSendMessageVO sendMessage(Long conversationId, ChatSendMessageDTO dto);
}
