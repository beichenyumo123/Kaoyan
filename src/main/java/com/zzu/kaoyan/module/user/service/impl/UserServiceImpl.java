package com.zzu.kaoyan.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.certification.entity.UserVerification;
import com.zzu.kaoyan.module.certification.mapper.UserVerificationMapper;
import com.zzu.kaoyan.module.membership.service.MembershipService;
import com.zzu.kaoyan.module.user.dto.UserUpdateDTO;
import com.zzu.kaoyan.module.user.dto.UserVO;
import com.zzu.kaoyan.module.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserVerificationMapper userVerificationMapper;
    private final MembershipService membershipService;

    public UserServiceImpl(UserMapper userMapper,
                           UserVerificationMapper userVerificationMapper,
                           MembershipService membershipService) {
        this.userMapper = userMapper;
        this.userVerificationMapper = userVerificationMapper;
        this.membershipService = membershipService;
    }

    @Override
    public UserVO getUserById(Long userId) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);

        // 2. 检查是否存在
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 3. 转换为 VO（不包含密码、deleted等敏感字段）
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);  // 同名属性自动复制

        // 4. 查询认证信息，填充 verifiedSchool / verifiedMajor
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            UserVerification verification = userVerificationMapper.selectOne(
                    Wrappers.<UserVerification>lambdaQuery()
                            .eq(UserVerification::getUserId, userId)
                            .eq(UserVerification::getStatus, 1)
            );
            if (verification != null) {
                vo.setVerifiedSchool(verification.getTargetSchool());
                vo.setVerifiedMajor(verification.getTargetMajor());
            }
        }

        // 5. 查询会员信息
        try {
            vo.setMembership(membershipService.getCurrentMembership(userId));
        } catch (Exception e) {
            // 会员信息查询失败不影响主流程
            vo.setMembership(null);
        }

        return vo;
    }

    @Override
    public UserVO updateCurrentUser(Long userId, UserUpdateDTO updateDTO) {
        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 2. 只更新允许修改的字段
        if (updateDTO.getUsername() != null) {
            // 检查用户名是否重复
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, updateDTO.getUsername())
                    .ne(User::getId, userId);  // 排除自己
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(400, "用户名已被占用");
            }
            user.setUsername(updateDTO.getUsername());
        }

        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }

        if (updateDTO.getTargetMajor() != null) {
            user.setTargetMajor(updateDTO.getTargetMajor());
        }

        if (updateDTO.getTargetSchool() != null) {
            user.setTargetSchool(updateDTO.getTargetSchool());
        }

        if (updateDTO.getPhone() != null) {
            // 检查手机号是否重复
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, updateDTO.getPhone())
                    .ne(User::getId, userId);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(400, "手机号已被占用");
            }
            user.setPhone(updateDTO.getPhone());
        }

        // 3. 保存更新
        userMapper.updateById(user);

        // 4. 返回更新后的用户信息
        return getUserById(userId);
    }

    @Override
    public void banUser(Long adminId, Long targetUserId) {
        // 1. 校验管理员
        User admin = userMapper.selectById(adminId);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            throw new BusinessException(403, "无管理员权限");
        }

        // 2. 查用户（只能查到未封禁的）
        User user = userMapper.selectById(targetUserId);

        // 3. 查到就封禁，查不到就不管
        if (user != null) {
            // 逻辑删除，自动 set is_deleted=1
            userMapper.deleteById(targetUserId);
        }
    }
    @Override
    public void updateUserRole(Long adminId, Long targetUserId, String role) {
        // 校验管理员
        User admin = userMapper.selectById(adminId);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            throw new BusinessException(403, "无管理员权限");
        }
        // 检查角色是否合法
        if (!"USER".equals(role) && !"MODERATOR".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(400, "无效的角色类型");
        }

        User user = new User();
        user.setId(targetUserId);
        user.setRole(role);
        userMapper.updateById(user);
    }
}