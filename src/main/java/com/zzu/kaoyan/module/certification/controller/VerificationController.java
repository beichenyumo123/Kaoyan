package com.zzu.kaoyan.module.certification.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.certification.dto.VerificationSubmitDTO;
import com.zzu.kaoyan.module.certification.service.VerificationService;
import com.zzu.kaoyan.module.certification.vo.VerificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certification")
@Tag(name = "上岸认证模块", description = "上岸认证申请与审核")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/submit")
    @Operation(summary = "提交认证申请")
    @SaCheckLogin
    public Result<VerificationVO> submit(@Valid @RequestBody VerificationSubmitDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(verificationService.submit(userId, dto));
    }

    @GetMapping("/status")
    @Operation(summary = "查询自己的认证状态")
    @SaCheckLogin
    public Result<VerificationVO> getMyStatus() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(verificationService.getMyStatus(userId));
    }

    @GetMapping("/list")
    @Operation(summary = "管理员分页查询认证列表")
    @SaCheckRole("ADMIN")
    public Result<Object> listByStatus(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(verificationService.listByStatus(status, pageNum, pageSize));
    }

    @PostMapping("/review/{id}")
    @Operation(summary = "管理员审核认证")
    @SaCheckRole("ADMIN")
    public Result<VerificationVO> review(
            @PathVariable("id") Long verificationId,
            @RequestParam Integer status,
            @RequestParam(required = false) String comment) {
        Long adminId = StpUtil.getLoginIdAsLong();
        return Result.success(verificationService.review(adminId, verificationId, status, comment));
    }
}