package com.zzu.kaoyan.module.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.mapper.PostMapper;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.certification.entity.UserVerification;
import com.zzu.kaoyan.module.certification.mapper.UserVerificationMapper;
import com.zzu.kaoyan.module.post.entity.Post;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理后台", description = "数据看板与管理接口")
public class AdminDashboardController {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final UserVerificationMapper verificationMapper;

    public AdminDashboardController(UserMapper userMapper,
                                    PostMapper postMapper,
                                    UserVerificationMapper verificationMapper) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.verificationMapper = verificationMapper;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "数据看板")
    @SaCheckRole("ADMIN")
    public Result<Map<String, Object>> dashboard() {
        long totalUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getDeleted, false));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long todayPosts = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getIsDeleted, 0)
                        .between(Post::getCreatedAt, todayStart, todayEnd));

        // 今日新增认证申请
        long todayVerifications = verificationMapper.selectCount(
                new LambdaQueryWrapper<UserVerification>()
                        .eq(UserVerification::getDeleted, false)
                        .between(UserVerification::getCreatedAt, todayStart, todayEnd));

        // 待审核认证数
        long pendingVerifications = verificationMapper.selectCount(
                new LambdaQueryWrapper<UserVerification>()
                        .eq(UserVerification::getDeleted, false)
                        .eq(UserVerification::getStatus, 0));

        List<Map<String, Object>> topActiveUsers = userMapper.selectTopActiveUsers(5);

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("todayPosts", todayPosts);
        data.put("todayVerifications", todayVerifications);
        data.put("pendingVerifications", pendingVerifications);
        data.put("topActiveUsers", topActiveUsers);
        return Result.success(data);
    }
}
