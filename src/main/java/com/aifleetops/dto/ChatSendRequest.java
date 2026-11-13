package com.aifleetops.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String content;
}
