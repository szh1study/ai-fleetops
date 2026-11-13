package com.aifleetops.ai.service.impl;

import com.aifleetops.ai.service.AiChatService;
import com.aifleetops.ai.tool.FleetOpsTools;
import com.aifleetops.entity.ChatMessage;
import com.aifleetops.entity.ChatSession;
import com.aifleetops.exception.BusinessException;
import com.aifleetops.service.ChatMessageService;
import com.aifleetops.service.ChatSessionService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 助手聊天编排实现（V0.6.1）。
 * <p>
 * 用 ChatClient + Tool Calling + ChatMemory 完成「对话 → 调工具 → 回填 → 回答」，
 * 同时把 user/assistant 文本落库为转录供前端回看。
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String CHAT_SYSTEM_PROMPT = """
            你是 AI FleetOps 的运营助手，帮助 Robotaxi 运营人员查询车队状态、异常、预测需求。
            只能依据工具返回的真实数据回答，不得虚构数据。

            规则：
            1. 回答简洁，优先给结论。
            2. 涉及调度执行时，只给建议，不自动执行（执行由人工确认）。
            3. 查询车辆/异常/预测时，务必调用对应工具获取真实数据，不要凭空编造数字。
            4. 天气数据为模拟数据（V0.6.1 未接真实 API）。
            5. queryForecast 只返回已有预测；查不到数据时明确告知用户先去「需求预测」页面生成，绝不编造预测数字。
            """;

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ChatMemory chatMemory;
    private final ChatClient chatClient;

    public AiChatServiceImpl(ChatClient.Builder chatClientBuilder,
                             ChatMemory chatMemory,
                             FleetOpsTools fleetOpsTools,
                             ChatSessionService chatSessionService,
                             ChatMessageService chatMessageService) {
        this.chatSessionService = chatSessionService;
        this.chatMessageService = chatMessageService;
        this.chatMemory = chatMemory;
        this.chatClient = chatClientBuilder
                .defaultSystem(CHAT_SYSTEM_PROMPT)
                .defaultTools(fleetOpsTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Override
    public ChatSession createSession() {
        ChatSession session = new ChatSession();
        chatSessionService.save(session);
        return session;
    }

    @Override
    public List<ChatSession> listSessions() {
        return chatSessionService.list(
                Wrappers.<ChatSession>lambdaQuery().orderByDesc(ChatSession::getUpdatedAt));
    }

    @Override
    public List<ChatMessage> getMessages(Long sessionId) {
        return chatMessageService.list(
                Wrappers.<ChatMessage>lambdaQuery()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getId));
    }

    @Override
    public ChatMessage sendMessage(Long sessionId, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(400, "消息内容不能为空");
        }
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null) {
            throw new BusinessException(404, "会话不存在: " + sessionId);
        }

        // 1. 持久化用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(content);
        chatMessageService.save(userMsg);

        // 2. 首条用户消息作为会话标题
        if (session.getTitle() == null || session.getTitle().isBlank()) {
            session.setTitle(content.length() > 20 ? content.substring(0, 20) : content);
            chatSessionService.updateById(session);
        }

        // 3. 调 AI（工具调用与多轮上下文由 ChatClient/ChatMemory 自动处理）
        String reply;
        try {
            reply = chatClient.prompt()
                    .user(content)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId.toString()))
                    .call()
                    .content();
            if (reply == null || reply.isBlank()) {
                reply = "（AI 未返回内容）";
            }
        } catch (RuntimeException e) {
            log.error("AI chat failed. sessionId={}", sessionId, e);
            reply = "AI 助手当前不可用，请稍后重试。";
        }

        // 4. 持久化 AI 回复
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        chatMessageService.save(assistantMsg);
        return assistantMsg;
    }

    @Override
    public void deleteSession(Long sessionId) {
        chatMessageService.remove(
                Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getSessionId, sessionId));
        chatSessionService.removeById(sessionId);
        chatMemory.clear(sessionId.toString());
    }
}
