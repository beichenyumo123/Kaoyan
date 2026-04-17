package com.zzu.kaoyan.module.interact.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interact.entity.ForumReport;
import com.zzu.kaoyan.module.interact.entity.dto.SubmitReportDTO;
// 假设你马上会建这个 Service
import com.zzu.kaoyan.module.interact.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzu.kaoyan.module.interact.entity.dto.ReportQueryDTO;

@RestController
@RequestMapping("/api/interact/report")
@RequiredArgsConstructor
@Tag(name = "互动模块-举报与审核接口")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/submit")
    @Operation(summary = "提交举报", description = "用户举报不良帖子、评论或用户。必须在请求头携带Token。")
    public Result<String> submitReport(@RequestBody SubmitReportDTO dto) {
        
        // 1. 参数基础校验 (可根据实际情况引入 @Validated 注解优化)
        if (dto.getTargetType() == null || dto.getTargetId() == null || dto.getReason() == null) {
            return Result.error("举报参数不完整");
        }

        // 2. 获取当前登录用户的ID (依赖 Sa-Token，记得在 Apifox 的 Header 传 satoken)
        Long reporterId = StpUtil.getLoginIdAsLong();

        // 3. 调用 Service 层执行入库逻辑
        // 注意：你需要去创建 ReportService 接口及其实现类来完成 save 操作
        reportService.submitReport(reporterId, dto);

        return Result.success("举报提交成功，感谢您对社区环境的维护！");
    }
    @GetMapping("/admin/list")
    @Operation(summary = "管理员获取举报列表 (分页/筛选)")
    // @SaCheckRole("ADMIN") // 建议配合1号同学的安全模块，限制仅管理员可访问
    public Result<Page<ForumReport>> getReportList(ReportQueryDTO queryDTO) {

        // 分页查询由于是 GET 请求，参数直接拼在 URL 上，不需要 @RequestBody
        Page<ForumReport> pageResult = reportService.getReportList(queryDTO);

        return Result.success(pageResult);
    }
}