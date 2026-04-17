package com.zzu.kaoyan.module.activity.controller;

import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.activity.service.RankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity/rank")
@RequiredArgsConstructor
@Tag(name = "排行榜接口")
public class RankController {

    private final RankService rankService;

    @Operation(summary = "积分总榜")
    @GetMapping("/total")
    public Result<List<Map<String, Object>>> total() {
        return Result.success(rankService.getTotalRank());
    }
}