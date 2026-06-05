package com.zzu.kaoyan.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.mapper.AuthMapper;
import com.zzu.kaoyan.module.auth.entity.RegisterDTO;
import com.zzu.kaoyan.module.auth.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import cn.hutool.crypto.digest.BCrypt;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthMapper authMapper;

    public AuthServiceImpl(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    @Override
    public User verifyAccountAndPassword(String account, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq(User::getEmail, account)
                .or()
                .eq(User::getPhone, account));

        User user = authMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException(400, "账号或密码错误");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public User register(RegisterDTO registerDTO) {
        checkUserExists(registerDTO);
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setRole("USER");
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

        authMapper.insert(user);

        user.setPassword(null);
        return user;
    }

    private void checkUserExists(RegisterDTO registerDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq(User::getEmail, registerDTO.getEmail())
                .or()
                .eq(User::getPhone, registerDTO.getPhone()));

        User existUser = authMapper.selectOne(queryWrapper);
        if (existUser != null) {
            if (registerDTO.getEmail().equals(existUser.getEmail())) {
                throw new BusinessException(400, "该邮箱已被注册");
            }
            if (registerDTO.getPhone() != null && registerDTO.getPhone().equals(existUser.getPhone())) {
                throw new BusinessException(400, "该手机号已被注册");
            }
        }
    }
}