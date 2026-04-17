package com.zzu.kaoyan.module.message.service;

import com.zzu.kaoyan.module.message.dto.MessageConversationVO;
import com.zzu.kaoyan.module.message.dto.MessageSendDTO;
import java.util.List;

public interface MessageService {

    boolean sendMessage(Long fromUserId, MessageSendDTO sendDTO);

    List<MessageConversationVO> getConversation(Long currentUserId, Long otherUserId);

    Long getUnreadCount(Long userId);

    boolean markAsRead(Long messageId, Long userId);
}