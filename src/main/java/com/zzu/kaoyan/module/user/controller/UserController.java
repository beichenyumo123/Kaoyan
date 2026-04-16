package com.zzu.kaoyan.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.user.dto.UserUpdateDTO;
import com.zzu.kaoyan.module.user.dto.UserVO;
import com.zzu.kaoyan.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户中心模块", description = "用户信息管理")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户信息
     * URL: GET /api/users/me
     * 需要登录
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    @SaCheckLogin  // 要求必须登录
    public Result<UserVO> getCurrentUser() {
        // StpUtil.getLoginId() 获取当前登录用户的ID（SaToken框架提供）
        Long userId = StpUtil.getLoginIdAsLong();
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 修改当前用户信息
     * URL: PUT /api/users/me
     * 需要登录
     */
    @PutMapping("/me")
    @Operation(summary = "修改个人信息")
    @SaCheckLogin
    public Result<UserVO> updateCurrentUser(@RequestBody UserUpdateDTO updateDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserVO userVO = userService.updateCurrentUser(userId, updateDTO);
        return Result.success(userVO);
    }

    /**
     * 获取其他用户公开主页
     * URL: GET /api/users/{userId}
     * 不需要登录（任何人都可以看）
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取其他用户公开信息")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 【管理员】封禁用户
     * URL: PUT /api/admin/users/{userId}/ban
     * 需要 ADMIN 角色
     */
    @PutMapping("/admin/users/{userId}/ban")
    @Operation(summary = "封禁用户（管理员）")
    public Result<Void> banUser(@PathVariable Long userId) {
        Long adminId = StpUtil.getLoginIdAsLong();
        userService.banUser(adminId, userId);
        return Result.success();
    }

    /**
     * 【管理员】修改用户角色
     * URL: PUT /api/admin/users/{userId}/role
     * 需要 ADMIN 角色
     */
    @PutMapping("/admin/users/{userId}/role")
    @Operation(summary = "修改用户角色（管理员）")
    public Result<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        Long adminId = StpUtil.getLoginIdAsLong();
        userService.updateUserRole(adminId, userId, role);
        return Result.success();
    }
}