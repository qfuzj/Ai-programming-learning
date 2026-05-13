package com.travel.advisor.controller.user;

import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.chat.ChatConversationCreateDTO;
import com.travel.advisor.dto.chat.ChatSendMessageDTO;
import com.travel.advisor.service.ConversationService;
import com.travel.advisor.service.MessageService;
import com.travel.advisor.vo.chat.ChatConversationVO;
import com.travel.advisor.vo.chat.ChatMessageVO;
import com.travel.advisor.vo.chat.ChatSendMessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/user/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @PostMapping("/conversations")
    public Result<Long> createConversation(@Valid @RequestBody ChatConversationCreateDTO dto) {
        return Result.success(conversationService.createConversation(dto));
    }

    @GetMapping("/conversations")
    public Result<List<ChatConversationVO>> listConversations() {
        return Result.success(conversationService.listMyConversations());
    }

    @GetMapping("/conversations/{conversationId}")
    public Result<ChatConversationVO> getConversation(@PathVariable Long conversationId) {
        return Result.success(conversationService.getConversation(conversationId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public Result<List<ChatMessageVO>> listMessages(@PathVariable Long conversationId) {
        return Result.success(messageService.listMessages(conversationId));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public Result<ChatSendMessageVO> sendMessage(@PathVariable Long conversationId,
                                                 @Valid @RequestBody ChatSendMessageDTO dto) {
        return Result.success(messageService.sendMessage(conversationId, dto));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(value = "/conversations/{conversationId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageStream(@PathVariable Long conversationId,
                                          @Valid @RequestBody ChatSendMessageDTO dto) {
        return messageService.sendMessageStream(conversationId, dto);
    }

    @DeleteMapping("/conversations/{conversationId}")
    public Result<Void> deleteConversation(@PathVariable Long conversationId) {
        conversationService.deleteConversation(conversationId);
        return Result.success();
    }
}
