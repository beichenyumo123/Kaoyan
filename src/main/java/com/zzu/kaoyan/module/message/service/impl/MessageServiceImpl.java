package com.zzu.kaoyan.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.mapper.MessageMapper;
import com.zzu.kaoyan.module.message.entity.Message;
import com.zzu.kaoyan.module.message.service.MessageService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    // 构造器注入（和 UserServiceImpl 风格一致）
    public MessageServiceImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public boolean sendMessage(Long fromUserId, Long toUserId, String content) {
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setIsRead(0);
        return messageMapper.insert(message) > 0;
    }

    @Override
    public List<Message> getConversation(Long currentUserId, Long otherUserId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(Message::getFromUserId, currentUserId)
                .eq(Message::getToUserId, otherUserId)
                .or()
                .eq(Message::getFromUserId, otherUserId)
                .eq(Message::getToUserId, currentUserId)
        ).orderByDesc(Message::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0);
        return messageMapper.selectCount(wrapper);
    }

    @Override
    public boolean markAsRead(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null || !message.getToUserId().equals(userId)) {
            return false;
        }
        message.setIsRead(1);
        return messageMapper.updateById(message) > 0;
    }
}