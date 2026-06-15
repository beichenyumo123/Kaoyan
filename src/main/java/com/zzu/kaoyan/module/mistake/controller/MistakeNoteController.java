package com.zzu.kaoyan.module.mistake.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.common.annotation.MembershipRequired;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.mistake.entity.dto.MarkdownRenderRequestDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteCreateDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteUpdateDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.PdfExportRequestDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.QuickSaveDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.ReviewCompleteDTO;
import com.zzu.kaoyan.module.mistake.entity.vo.*;
import com.zzu.kaoyan.module.mistake.service.MistakeNotePdfService;
import com.zzu.kaoyan.module.mistake.service.MistakeNoteService;
import com.zzu.kaoyan.module.mistake.service.MistakeNotificationService;
import com.zzu.kaoyan.module.mistake.service.OCRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mistake")
@RequiredArgsConstructor
@Tag(name = "智能错题本", description = "OCR 智能错题本 + 艾宾浩斯复习计划 —— 拍照→OCR→知识点定位→入错题本，按遗忘曲线自动推送复习")
public class MistakeNoteController {

    private final MistakeNoteService mistakeNoteService;
    private final OCRService ocrService;
    private final MistakeNotePdfService pdfService;
    private final MistakeNotificationService notificationService;

    @Operation(
            summary = "OCR 识别题目图片",
            description = "上传题目图片URL（先调 /api/upload/image 上传获取），返回OCR识别的文字内容和建议的科目/知识点。使用PaddleOCR引擎，对中文数学公式支持较好。"
    )
    @PostMapping("/ocr")
    @MembershipRequired("ocr")
    public Result<OCRResultVO> ocr(
            @Parameter(description = "图片URL，来自上传接口返回", required = true, example = "/uploads/images/202605/abc123.jpg")
            @RequestParam String imageUrl,
            @Parameter(description = "可选的科目提示，帮助OCR精准识别", example = "408计算机")
            @RequestParam(required = false) String subject) {
        return Result.success(ocrService.recognize(imageUrl, subject));
    }

    @Operation(
            summary = "创建错题",
            description = "将OCR识别结果或手动输入的题目保存到错题本。创建后自动设置首次复习日期=明天（艾宾浩斯第0阶段）。"
    )
    @PostMapping("/notes")
    public Result<MistakeNoteVO> create(@Valid @RequestBody MistakeNoteCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.create(dto, userId));
    }

    @Operation(summary = "从 AI 对话快速收藏", description = "将 AI 答疑中的对话快速保存到错题本，支持去重检查")
    @PostMapping("/quick-save")
    public Result<Map<String, Object>> quickSave(@Valid @RequestBody QuickSaveDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.quickSave(dto, userId));
    }

    @Operation(summary = "批量检查消息是否已收藏", description = "传入 AI 消息ID列表，返回已收藏的ID集合，用于按钮状态显示")
    @PostMapping("/check-saved")
    public Result<Map<String, Object>> checkSaved(@RequestBody Map<String, List<Long>> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> chatMessageIds = body.get("chatMessageIds");
        List<Long> savedIds = mistakeNoteService.checkSaved(chatMessageIds, userId);
        return Result.success(Map.of("savedIds", savedIds));
    }

    @Operation(summary = "更新错题", description = "修改错题内容、答案、知识点等。传什么改什么，不传的字段保持不变。")
    @PutMapping("/notes")
    public Result<MistakeNoteVO> update(@Valid @RequestBody MistakeNoteUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.update(dto.getId(), dto, userId));
    }

    @Operation(summary = "删除错题（逻辑删除）", description = "逻辑删除，不物理删除数据。删除后不再参与复习计划。")
    @DeleteMapping("/notes/{id}")
    public Result<String> delete(
            @Parameter(description = "错题ID", required = true) @PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        mistakeNoteService.delete(id, userId);
        return Result.success("ok");
    }

    @Operation(summary = "查看错题详情", description = "查看单题详情，可选渲染 Markdown 为 HTML")
    @GetMapping("/notes/{id}")
    public Result<MistakeNoteVO> detail(
            @Parameter(description = "错题ID", required = true) @PathVariable Long id,
            @Parameter(description = "是否渲染Markdown为HTML，默认false")
            @RequestParam(defaultValue = "false") boolean renderHtml) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.getById(id, userId, renderHtml));
    }

    @Operation(summary = "Markdown 渲染", description = "将 Markdown 文本实时渲染为 HTML，用于前端编辑器预览")
    @PostMapping("/markdown/render")
    public Result<MarkdownRenderVO> renderMarkdown(@Valid @RequestBody MarkdownRenderRequestDTO dto) {
        String html = mistakeNoteService.renderMarkdown(dto.getMarkdown());
        return Result.success(new MarkdownRenderVO(html));
    }

    @Operation(
            summary = "分页查询错题本",
            description = "查询当前用户的所有错题，支持按科目筛选。按创建时间倒序排列。"
    )
    @GetMapping("/notes")
    public Result<PageInfo<MistakeNoteVO>> page(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "科目筛选，不传则查全部", example = "408计算机")
            @RequestParam(required = false) String subject) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.page(pageNum, pageSize, subject, userId));
    }

    @Operation(
            summary = "今日待复习错题列表",
            description = "获取根据艾宾浩斯遗忘曲线计算的今日应复习的错题。每日7:00定时任务自动生成计划，也可手动调用此接口触发生成。"
    )
    @GetMapping("/review/today")
    public Result<PageInfo<ReviewTaskVO>> todayReview(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.getTodayReviewTasks(userId, pageNum, pageSize));
    }

    @Operation(
            summary = "完成一道错题的复习",
            description = "标记一道错题复习完成，传入答对/答错和掌握程度。系统自动：记录复习日志 → 更新掌握度 → 推进艾宾浩斯阶段 → 计算下次复习日期 → 同步每日计划进度。"
    )
    @PostMapping("/review/{noteId}/complete")
    public Result<ReviewResultVO> completeReview(
            @Parameter(description = "错题ID", required = true) @PathVariable Long noteId,
            @Valid @RequestBody ReviewCompleteDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        ReviewResultVO result = mistakeNoteService.completeReview(noteId, dto.getMasteryAfter(), dto.getIsCorrect(), userId);
        return Result.success(result);
    }

    @Operation(
            summary = "错题本统计信息",
            description = "返回：总错题数、今日待复习数/已完成数、平均掌握度、科目分布、艾宾浩斯阶段分布。"
    )
    @GetMapping("/stats")
    public Result<MistakeStatsVO> stats() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.getStats(userId));
    }

    @Operation(
            summary = "月度复习日历",
            description = "返回指定月份每天的计划复习数/已完成数，用于前端日历视图。"
    )
    @GetMapping("/calendar")
    public Result<CalendarMonthVO> calendar(
            @Parameter(description = "年份", required = true, example = "2026")
            @RequestParam int year,
            @Parameter(description = "月份 1-12", required = true, example = "6")
            @RequestParam int month) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.getCalendarMonth(userId, year, month));
    }

    @Operation(summary = "指定日期的待复习列表", description = "点击日历中的某一天，查看当天待复习错题")
    @GetMapping("/calendar/{date}/notes")
    public Result<PageInfo<ReviewTaskVO>> calendarDayNotes(
            @Parameter(description = "日期 yyyy-MM-dd", required = true, example = "2026-06-04")
            @PathVariable String date,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        LocalDate localDate = LocalDate.parse(date);
        return Result.success(mistakeNoteService.getCalendarDayNotes(userId, localDate, pageNum, pageSize));
    }

    @Operation(
            summary = "艾宾浩斯遗忘曲线统计",
            description = "返回阶段分布（柱状图）、每日准确率趋势（折线图）、掌握度分布（饼图）"
    )
    @GetMapping("/stats/ebbinghaus")
    @MembershipRequired("ebbinghaus_stats")
    public Result<EbbinghausStatsVO> ebbinghausStats(
            @Parameter(description = "准确率趋势统计天数，默认30天", example = "30")
            @RequestParam(defaultValue = "30") int days) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.getEbbinghausStats(userId, days));
    }

    @Operation(summary = "导出错题 PDF", description = "将选中的错题导出为 A4 PDF 文档，支持中文。")
    @PostMapping("/export/pdf")
    @MembershipRequired("export_pdf")
    public void exportPdf(@Valid @RequestBody PdfExportRequestDTO dto,
                          HttpServletResponse response) throws IOException {
        Long userId = StpUtil.getLoginIdAsLong();
        byte[] pdfBytes = pdfService.exportNotes(dto, userId);

        String filename = "错题本_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(filename, "UTF-8"));
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    // ==================== 通知相关 ====================

    @Operation(summary = "通知列表", description = "分页获取当前用户的通知，按时间倒序")
    @GetMapping("/notifications")
    public Result<PageInfo<MistakeNotificationVO>> notifications(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(notificationService.list(userId, pageNum, pageSize));
    }

    @Operation(summary = "未读通知数", description = "获取未读通知数量，用于前端小红点/角标")
    @GetMapping("/notifications/unread-count")
    public Result<java.util.Map<String, Integer>> unreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(java.util.Map.of("count", notificationService.unreadCount(userId)));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/notifications/{id}/read")
    public Result<String> markRead(
            @Parameter(description = "通知ID", required = true) @PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        notificationService.markRead(id, userId);
        return Result.success("ok");
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/notifications/read-all")
    public Result<String> markAllRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        notificationService.markAllRead(userId);
        return Result.success("ok");
    }
}
