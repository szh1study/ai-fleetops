package com.aifleetops.ai.service;

import com.aifleetops.entity.ChatMessage;
import com.aifleetops.entity.ChatSession;

import java.util.List;

/**
 * AI 运营助手聊天服务（V0.6.1）。
 */
public interface AiChatService {

    ChatSession createSession();

    List<ChatSession> listSessions();

    List<ChatMessage> getMessages(Long sessionId);

    /**
     * 发送用户消息并返回 AI 回复（AI 失败时返回友好提示文本）。
     */
    ChatMessage sendMessage(Long sessionId, String content);

    void deleteSession(Long sessionId);
}
