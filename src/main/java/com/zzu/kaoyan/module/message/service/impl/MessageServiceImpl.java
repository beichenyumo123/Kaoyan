package com.zzu.kaoyan.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.mapper.MessageMapper;
import com.zzu.kaoyan.module.message.dto.MessageConversationVO;
import com.zzu.kaoyan.module.message.dto.MessageSendDTO;
import com.zzu.kaoyan.module.message.entity.Message;  // ⚠️ 必须导入这行
import com.zzu.kaoyan.module.message.service.MessageService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    public MessageServiceImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public boolean sendMessage(Long fromUserId, MessageSendDTO sendDTO) {
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(sendDTO.getToUserId());
        message.setContent(sendDTO.getContent());
        message.setIsRead(0);
        return messageMapper.insert(message) > 0;
    }

    @Override
    public List<MessageConversationVO> getConversation(Long currentUserId, Long otherUserId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(Message::getFromUserId, currentUserId)
                .eq(Message::getToUserId, otherUserId)
                .or()
                .eq(Message::getFromUserId, otherUserId)
                .eq(Message::getToUserId, currentUserId)
        ).orderByDesc(Message::getCreateTime);

        List<Message> messages = messageMapper.selectList(wrapper);

        return messages.stream()
                .map(this::convertToConversationVO)
                .collect(Collectors.toList());
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

    private MessageConversationVO convertToConversationVO(Message message) {
        MessageConversationVO vo = new MessageConversationVO();
        vo.setId(message.getId());
        vo.setFromUserId(message.getFromUserId());
        vo.setToUserId(message.getToUserId());
        vo.setContent(message.getContent());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}