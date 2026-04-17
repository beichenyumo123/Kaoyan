package com.zzu.kaoyan.module.user.service;

import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.module.user.dto.UserUpdateDTO;
import com.zzu.kaoyan.module.user.dto.UserVO;

public interface UserService {

    // 根据ID获取用户信息（返回VO，不包含敏感字段）
    UserVO getUserById(Long userId);

    // 更新当前用户信息
    UserVO updateCurrentUser(Long userId, UserUpdateDTO updateDTO);

    // 管理员：封禁用户（逻辑删除）
    void banUser(Long adminId, Long targetUserId);

    // 管理员：修改用户角色
    void updateUserRole(Long adminId, Long targetUserId, String role);
}