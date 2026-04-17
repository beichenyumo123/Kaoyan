package com.zzu.kaoyan.module.activity.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.activity.entity.dto.CheckInDTO;
import com.zzu.kaoyan.module.activity.entity.vo.CheckInVO;
import com.zzu.kaoyan.module.activity.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity/checkin")
@RequiredArgsConstructor
@Tag(name = "打卡接口")
public class CheckInController {

    private final CheckInService checkInService;

    @Operation(summary = "每日打卡")
    @PostMapping
    public Result<CheckInVO> checkIn(@Valid @RequestBody CheckInDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(checkInService.checkIn(dto, userId));
    }

    @Operation(summary = "今日是否打卡")
    @GetMapping("/today")
    public Result<Boolean> today() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(checkInService.isCheckedToday(userId));
    }

    @Operation(summary = "个人打卡统计")
    @GetMapping("/stats")
    public Result<CheckInVO> stats() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(checkInService.getUserStats(userId));
    }
}