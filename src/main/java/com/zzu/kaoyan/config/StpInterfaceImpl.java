package com.zzu.kaoyan.config;

import cn.dev33.satoken.stp.StpInterface;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.mapper.AuthMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限扩展接口实现
 * 从数据库加载用户的角色和权限列表
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final AuthMapper authMapper;

    public StpInterfaceImpl(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 暂不需要细粒度权限，返回空列表
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = authMapper.selectById(Long.valueOf(loginId.toString()));
        if (user == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(user.getRole());
    }
}
