package com.aifleetops.controller;

import com.aifleetops.ai.service.AiChatService;
import com.aifleetops.common.Result;
import com.aifleetops.dto.ChatSendRequest;
import com.aifleetops.entity.ChatMessage;
import com.aifleetops.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 助手聊天接口（V0.6.1）。
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiChatService aiChatService;

    @PostMapping("/sessions")
    public Result<ChatSession> createSession() {
        return Result.success(aiChatService.createSession());
    }

    @GetMapping("/sessions")
    public Result<List<ChatSession>> listSessions() {
        return Result.success(aiChatService.listSessions());
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<List<ChatMessage>> messages(@PathVariable Long id) {
        return Result.success(aiChatService.getMessages(id));
    }

    @PostMapping("/sessions/{id}/messages")
    public Result<ChatMessage> send(@PathVariable Long id, @RequestBody ChatSendRequest request) {
        return Result.success(aiChatService.sendMessage(id, request.getContent()));
    }

    @DeleteMapping("/sessions/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aiChatService.deleteSession(id);
        return Result.success();
    }
}
