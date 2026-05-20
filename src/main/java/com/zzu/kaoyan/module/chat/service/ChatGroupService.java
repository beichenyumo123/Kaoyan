package com.zzu.kaoyan.module.chat.service;

import com.zzu.kaoyan.module.chat.dto.CreateGroupDTO;
import com.zzu.kaoyan.module.chat.vo.ChatGroupVO;

import java.util.List;

public interface ChatGroupService {
    Long createGroup(CreateGroupDTO dto, Long ownerId);
    List<ChatGroupVO> listMyGroups(Long userId);
    ChatGroupVO getGroupDetail(Long groupId);
    void joinGroup(Long groupId, Long userId);
    void leaveGroup(Long groupId, Long userId);
    boolean isMember(Long groupId, Long userId);
}
