package com.zzu.kaoyan.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.util.SensitiveWordUtil;
import com.zzu.kaoyan.mapper.MessageMapper;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.message.dto.MessageContactVO;
import com.zzu.kaoyan.module.message.dto.MessageConversationVO;
import com.zzu.kaoyan.module.message.dto.MessageSendDTO;
import com.zzu.kaoyan.module.message.entity.Message;
import com.zzu.kaoyan.module.message.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    public MessageServiceImpl(MessageMapper messageMapper, UserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    @Override
    public boolean sendMessage(Long fromUserId, MessageSendDTO sendDTO) {
        // 敏感词过滤
        SensitiveWordUtil.FilterResult result = SensitiveWordUtil.filter(sendDTO.getContent());
        if (result.isHasSensitive()) {
            log.warn("私信包含敏感词 — fromUserId={}, matched={}", fromUserId, result.getMatched());
        }

        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(sendDTO.getToUserId());
        message.setContent(result.getFilteredText());
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
        if (messages.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询用户信息
        User currentUser = userMapper.selectById(currentUserId);
        User otherUser = userMapper.selectById(otherUserId);

        return messages.stream()
                .map(msg -> convertToConversationVO(msg, currentUser, otherUser))
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

    @Override
    public List<MessageContactVO> getContactList(Long userId) {
        // 查询与该用户有私信往来的所有消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(Message::getFromUserId, userId)
                .or()
                .eq(Message::getToUserId, userId)
        );
        List<Message> messages = messageMapper.selectList(wrapper);

        // 收集所有对话过的用户ID
        Set<Long> contactIds = new HashSet<>();
        for (Message msg : messages) {
            if (msg.getFromUserId().equals(userId)) {
                contactIds.add(msg.getToUserId());
            } else {
                contactIds.add(msg.getFromUserId());
            }
        }

        if (contactIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询这些用户的信息
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(User::getId, contactIds);
        List<User> users = userMapper.selectList(userWrapper);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        // 构建返回结果
        List<MessageContactVO> result = new ArrayList<>();
        for (Long contactId : contactIds) {
            User contact = userMap.get(contactId);
            if (contact == null) continue;

            MessageContactVO vo = new MessageContactVO();
            vo.setUserId(contact.getId());
            vo.setUsername(contact.getUsername());
            vo.setAvatarUrl(contact.getAvatarUrl());

            // 查询最后一条消息
            LambdaQueryWrapper<Message> lastMsgWrapper = new LambdaQueryWrapper<>();
            lastMsgWrapper.and(w -> w
                    .eq(Message::getFromUserId, userId)
                    .eq(Message::getToUserId, contactId)
                    .or()
                    .eq(Message::getFromUserId, contactId)
                    .eq(Message::getToUserId, userId)
            ).orderByDesc(Message::getCreateTime).last("LIMIT 1");
            Message lastMsg = messageMapper.selectOne(lastMsgWrapper);

            if (lastMsg != null) {
                vo.setLastMessage(lastMsg.getContent());
                vo.setLastMessageTime(lastMsg.getCreateTime());
            }

            // 查询未读消息数
            LambdaQueryWrapper<Message> unreadWrapper = new LambdaQueryWrapper<>();
            unreadWrapper.eq(Message::getFromUserId, contactId)
                    .eq(Message::getToUserId, userId)
                    .eq(Message::getIsRead, 0);
            vo.setUnreadCount(messageMapper.selectCount(unreadWrapper));

            result.add(vo);
        }

        // 按最后消息时间排序
        result.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return result;
    }

    private MessageConversationVO convertToConversationVO(Message msg, User currentUser, User otherUser) {
        MessageConversationVO vo = new MessageConversationVO();
        vo.setId(msg.getId());
        vo.setFromUserId(msg.getFromUserId());
        vo.setToUserId(msg.getToUserId());
        vo.setContent(msg.getContent());
        vo.setIsRead(msg.getIsRead());
        vo.setCreateTime(msg.getCreateTime());

        // 根据发送者ID设置用户名和头像
        if (msg.getFromUserId().equals(currentUser.getId())) {
            vo.setFromUsername(currentUser.getUsername());
            vo.setFromAvatarUrl(currentUser.getAvatarUrl());
            vo.setToUsername(otherUser.getUsername());
            vo.setToAvatarUrl(otherUser.getAvatarUrl());
        } else {
            vo.setFromUsername(otherUser.getUsername());
            vo.setFromAvatarUrl(otherUser.getAvatarUrl());
            vo.setToUsername(currentUser.getUsername());
            vo.setToAvatarUrl(currentUser.getAvatarUrl());
        }

        return vo;
    }
}