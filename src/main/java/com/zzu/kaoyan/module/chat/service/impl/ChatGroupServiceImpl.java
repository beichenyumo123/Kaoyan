package com.zzu.kaoyan.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.chat.dto.CreateGroupDTO;
import com.zzu.kaoyan.module.chat.entity.ChatGroup;
import com.zzu.kaoyan.module.chat.entity.GroupMember;
import com.zzu.kaoyan.module.chat.mapper.ChatGroupMapper;
import com.zzu.kaoyan.module.chat.mapper.GroupMemberMapper;
import com.zzu.kaoyan.module.chat.service.ChatGroupService;
import com.zzu.kaoyan.module.chat.vo.ChatGroupVO;
import com.zzu.kaoyan.module.chat.vo.GroupMemberVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatGroupServiceImpl implements ChatGroupService {

    private final ChatGroupMapper chatGroupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;

    public ChatGroupServiceImpl(ChatGroupMapper chatGroupMapper,
                                GroupMemberMapper groupMemberMapper,
                                UserMapper userMapper) {
        this.chatGroupMapper = chatGroupMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(CreateGroupDTO dto, Long ownerId) {
        ChatGroup group = new ChatGroup();
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setOwnerId(ownerId);
        group.setMemberCount(1);
        group.setIsDeleted(0);
        chatGroupMapper.insert(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(ownerId);
        member.setRole("OWNER");
        member.setIsDeleted(0);
        groupMemberMapper.insert(member);

        return group.getId();
    }

    @Override
    public List<ChatGroupVO> listMyGroups(Long userId) {
        List<GroupMember> memberships = groupMemberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getUserId, userId)
                        .eq(GroupMember::getIsDeleted, 0));

        if (memberships.isEmpty()) return new ArrayList<>();

        List<Long> groupIds = memberships.stream().map(GroupMember::getGroupId).toList();
        List<ChatGroup> groups = chatGroupMapper.selectBatchIds(groupIds);
        if (groups.isEmpty()) return new ArrayList<>();

        List<Long> ownerIds = groups.stream().map(ChatGroup::getOwnerId).distinct().toList();
        List<User> owners = userMapper.selectBatchIds(ownerIds);
        Map<Long, String> ownerNameMap = owners.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));

        return groups.stream().map(g -> {
            ChatGroupVO vo = new ChatGroupVO();
            vo.setId(g.getId());
            vo.setName(g.getName());
            vo.setDescription(g.getDescription());
            vo.setAvatarUrl(g.getAvatarUrl());
            vo.setOwnerId(g.getOwnerId());
            vo.setOwnerName(ownerNameMap.getOrDefault(g.getOwnerId(), "未知"));
            vo.setMemberCount(g.getMemberCount());
            vo.setCreatedAt(g.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ChatGroupVO getGroupDetail(Long groupId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException(404, "群组不存在");
        }

        User owner = userMapper.selectById(group.getOwnerId());

        ChatGroupVO vo = new ChatGroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setDescription(group.getDescription());
        vo.setAvatarUrl(group.getAvatarUrl());
        vo.setOwnerId(group.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getUsername() : "未知");
        vo.setMemberCount(group.getMemberCount());
        vo.setCreatedAt(group.getCreatedAt());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinGroup(Long groupId, Long userId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BusinessException(404, "群组不存在");
        }

        Long count = groupMemberMapper.selectCount(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userId)
                        .eq(GroupMember::getIsDeleted, 0));
        if (count > 0) {
            throw new BusinessException(400, "你已在群组中");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setIsDeleted(0);
        groupMemberMapper.insert(member);

        group.setMemberCount(group.getMemberCount() + 1);
        chatGroupMapper.updateById(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember member = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userId)
                        .eq(GroupMember::getIsDeleted, 0));
        if (member == null) {
            throw new BusinessException(400, "你不在该群组中");
        }
        if ("OWNER".equals(member.getRole())) {
            throw new BusinessException(400, "群主不能退群，请先转让群主");
        }

        member.setIsDeleted(1);
        groupMemberMapper.updateById(member);

        ChatGroup group = chatGroupMapper.selectById(groupId);
        group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
        chatGroupMapper.updateById(group);
    }

    @Override
    public boolean isMember(Long groupId, Long userId) {
        Long count = groupMemberMapper.selectCount(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userId)
                        .eq(GroupMember::getIsDeleted, 0));
        return count > 0;
    }
}
