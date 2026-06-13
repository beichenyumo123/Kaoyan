package com.zzu.kaoyan.module.schoolselect.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.annotation.MembershipRequired;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.schoolselect.entity.dto.RecommendationRequestDTO;
import com.zzu.kaoyan.module.schoolselect.entity.po.SchoolInfo;
import com.zzu.kaoyan.module.schoolselect.entity.vo.RecommendationResultVO;
import com.zzu.kaoyan.module.schoolselect.service.SchoolSelectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school-select")
@RequiredArgsConstructor
@Tag(name = "AI智能择校引擎")
public class SchoolSelectController {

    private final SchoolSelectService schoolSelectService;

    @Operation(summary = "获取择校推荐")
    @PostMapping("/recommend")
    @MembershipRequired("school_recommend")
    public Result<RecommendationResultVO> recommend(@Valid @RequestBody RecommendationRequestDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(schoolSelectService.recommend(dto, userId));
    }

    @Operation(summary = "获取推荐历史")
    @GetMapping("/history")
    public Result<List<RecommendationResultVO>> getHistory() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(schoolSelectService.getHistory(userId));
    }

    @Operation(summary = "查询院校列表")
    @GetMapping("/schools")
    public Result<List<SchoolInfo>> listSchools(@RequestParam(required = false) String keyword) {
        return Result.success(schoolSelectService.listSchools(keyword));
    }
}
