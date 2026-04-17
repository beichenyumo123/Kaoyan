package com.zzu.kaoyan.module.message.service;

import com.zzu.kaoyan.module.message.entity.Message;
import java.util.List;

public interface MessageService {

    boolean sendMessage(Long fromUserId, Long toUserId, String content);

    List<Message> getConversation(Long currentUserId, Long otherUserId);

    Long getUnreadCount(Long userId);

    boolean markAsRead(Long messageId, Long userId);
}