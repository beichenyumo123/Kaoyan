package com.zzu.kaoyan.module.interact.controller;

import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interact.service.UserStatsService;
import com.zzu.kaoyan.module.interact.entity.vo.UserStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interact/stats")
@RequiredArgsConstructor
@Tag(name = "互动模块-用户统计", description = "获取用户发帖数、获赞数等统计数据")
public class UserStatsController {

    private final UserStatsService userStatsService;

    @Operation(summary = "获取用户统计数据", description = "根据用户ID查询其发帖总数和被点赞总数")
    @GetMapping("/{userId}")
    public Result<UserStatsVO> getUserStats(@PathVariable Long userId) {
        UserStatsVO stats = userStatsService.getUserStats(userId);
        return Result.success(stats);
    }
}