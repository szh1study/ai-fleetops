package com.aifleetops.service.impl;

import com.aifleetops.entity.ChatMessage;
import com.aifleetops.mapper.ChatMessageMapper;
import com.aifleetops.service.ChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {
}
