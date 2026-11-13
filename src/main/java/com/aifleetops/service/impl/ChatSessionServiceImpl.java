package com.aifleetops.service.impl;

import com.aifleetops.entity.ChatSession;
import com.aifleetops.mapper.ChatSessionMapper;
import com.aifleetops.service.ChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
        implements ChatSessionService {
}
