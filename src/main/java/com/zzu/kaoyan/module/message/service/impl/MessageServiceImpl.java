package com.zzu.kaoyan.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzu.kaoyan.module.message.entity.Message;
import com.zzu.kaoyan.module.message.mapper.MessageMapper;
import com.zzu.kaoyan.module.message.service.MessageService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public boolean sendMessage(Long fromUserId, Long toUserId, String content) {
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setIsRead(0);
        return save(message);
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
        return list(wrapper);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0);
        return count(wrapper);
    }

    @Override
    public boolean markAsRead(Long messageId, Long userId) {
        Message message = getById(messageId);
        if (message == null || !message.getToUserId().equals(userId)) {
            return false;
        }
        message.setIsRead(1);
        return updateById(message);
    }
}