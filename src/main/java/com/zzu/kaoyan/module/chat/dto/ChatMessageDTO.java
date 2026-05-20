package com.zzu.kaoyan.module.chat.dto;

import lombok.Data;

/**
 * WebSocket 客户端发送的群聊消息 JSON 格式。
 */
@Data
public class ChatMessageDTO {
    private String content;
}
