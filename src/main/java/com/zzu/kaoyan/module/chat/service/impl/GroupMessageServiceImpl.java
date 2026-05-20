package com.zzu.kaoyan.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.chat.entity.GroupMessage;
import com.zzu.kaoyan.module.chat.mapper.GroupMessageMapper;
import com.zzu.kaoyan.module.chat.service.GroupMessageService;
import com.zzu.kaoyan.module.chat.vo.ChatMessageVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupMessageServiceImpl implements GroupMessageService {

    private final GroupMessageMapper groupMessageMapper;
    private final UserMapper userMapper;

    public GroupMessageServiceImpl(GroupMessageMapper groupMessageMapper, UserMapper userMapper) {
        this.groupMessageMapper = groupMessageMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void saveMessage(GroupMessage message) {
        groupMessageMapper.insert(message);
    }

    @Override
    public List<ChatMessageVO> getHistory(Long groupId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<GroupMessage> messages = groupMessageMapper.selectList(
                new LambdaQueryWrapper<GroupMessage>()
                        .eq(GroupMessage::getGroupId, groupId)
                        .eq(GroupMessage::getIsDeleted, 0)
                        .orderByDesc(GroupMessage::getCreatedAt));

        if (messages.isEmpty()) return new ArrayList<>();

        List<Long> userIds = messages.stream().map(GroupMessage::getUserId).distinct().toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        return messages.stream().map(m -> {
            ChatMessageVO vo = new ChatMessageVO();
            vo.setId(m.getId());
            vo.setGroupId(m.getGroupId());
            vo.setUserId(m.getUserId());
            User u = userMap.get(m.getUserId());
            vo.setUsername(u != null ? u.getUsername() : "已注销");
            vo.setAvatarUrl(u != null ? u.getAvatarUrl() : null);
            vo.setContent(m.getContent());
            vo.setCreatedAt(m.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }
}
