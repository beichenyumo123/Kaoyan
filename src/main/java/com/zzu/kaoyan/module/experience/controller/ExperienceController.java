package com.zzu.kaoyan.module.experience.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.experience.dto.ExperiencePostDTO;
import com.zzu.kaoyan.module.experience.service.ExperienceService;
import com.zzu.kaoyan.module.experience.vo.ExperiencePostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/experience")
@Tag(name = "经验贴模块", description = "结构化经验贴发布、检索、互动")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @PostMapping("/posts")
    @Operation(summary = "创建经验贴")
    @SaCheckLogin
    public Result<ExperiencePostVO> create(@Valid @RequestBody ExperiencePostDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(experienceService.create(userId, dto));
    }

    @PutMapping("/posts/{id}")
    @Operation(summary = "编辑经验贴")
    @SaCheckLogin
    public Result<ExperiencePostVO> update(@PathVariable Long id,
                                           @Valid @RequestBody ExperiencePostDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(experienceService.update(userId, id, dto));
    }

    @GetMapping("/posts/{id}")
    @Operation(summary = "经验贴详情")
    public Result<ExperiencePostVO> getDetail(@PathVariable Long id) {
        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return Result.success(experienceService.getDetail(id, currentUserId));
    }

    @DeleteMapping("/posts/{id}")
    @Operation(summary = "删除经验贴")
    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        experienceService.delete(userId, id);
        return Result.success();
    }

    @GetMapping("/posts")
    @Operation(summary = "经验贴分页列表")
    public Result<Object> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer isVerified,
            @RequestParam(required = false) String targetSchool,
            @RequestParam(required = false) String undergradSchool) {
        return Result.success(experienceService.list(pageNum, pageSize, isVerified, targetSchool, undergradSchool));
    }

    @GetMapping("/posts/search")
    @Operation(summary = "精准检索经验贴（择校引擎）")
    public Result<Object> search(
            @RequestParam(required = false) String undergradSchool,
            @RequestParam(required = false) String targetSchool,
            @RequestParam(required = false) BigDecimal minScore,
            @RequestParam(required = false) BigDecimal maxScore,
            @RequestParam(required = false) Integer isVerified,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(experienceService.search(undergradSchool, targetSchool,
                minScore, maxScore, isVerified, pageNum, pageSize));
    }

    @PostMapping("/posts/{id}/like")
    @Operation(summary = "点赞/取消点赞")
    @SaCheckLogin
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(experienceService.toggleLike(id, userId));
    }

    @PostMapping("/posts/{id}/collect")
    @Operation(summary = "收藏/取消收藏")
    @SaCheckLogin
    public Result<Boolean> toggleCollect(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(experienceService.toggleCollect(id, userId));
    }

    @GetMapping("/my-collects")
    @Operation(summary = "我的收藏列表")
    @SaCheckLogin
    public Result<Object> myCollects(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(experienceService.myCollects(userId, pageNum, pageSize));
    }

    @GetMapping("/users/{userId}/posts")
    @Operation(summary = "某用户的经验贴列表")
    public Result<Object> listByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(experienceService.listByUserId(userId, pageNum, pageSize));
    }
}