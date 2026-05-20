package com.zzu.kaoyan.module.chat.service;

import com.zzu.kaoyan.module.chat.entity.GroupMessage;
import com.zzu.kaoyan.module.chat.vo.ChatMessageVO;

import java.util.List;

public interface GroupMessageService {
    void saveMessage(GroupMessage message);
    List<ChatMessageVO> getHistory(Long groupId, int pageNum, int pageSize);
}
