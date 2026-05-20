package com.zzu.kaoyan.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.chat.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
}
