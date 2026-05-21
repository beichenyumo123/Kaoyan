package com.zzu.kaoyan.module.mistake.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteCreateDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteUpdateDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.ReviewCompleteDTO;
import com.zzu.kaoyan.module.mistake.entity.vo.MistakeNoteVO;
import com.zzu.kaoyan.module.mistake.entity.vo.MistakeStatsVO;
import com.zzu.kaoyan.module.mistake.entity.vo.OCRResultVO;
import com.zzu.kaoyan.module.mistake.entity.vo.ReviewResultVO;
import com.zzu.kaoyan.module.mistake.entity.vo.ReviewTaskVO;
import com.zzu.kaoyan.module.mistake.service.MistakeNoteService;
import com.zzu.kaoyan.module.mistake.service.OCRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mistake")
@RequiredArgsConstructor
@Tag(name = "智能错题本", description = "OCR 智能错题本 + 艾宾浩斯复习计划 —— 拍照→OCR→知识点定位→入错题本，按遗忘曲线自动推送复习")
public class MistakeNoteController {

    private final MistakeNoteService mistakeNoteService;
    private final OCRService ocrService;

    @Operation(
            summary = "OCR 识别题目图片",
            description = "上传题目图片URL（先调 /api/upload/image 上传获取），返回OCR识别的文字内容和建议的科目/知识点。使用PaddleOCR引擎，对中文数学公式支持较好。"
    )
    @PostMapping("/ocr")
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

    @Operation(summary = "查看错题详情")
    @GetMapping("/notes/{id}")
    public Result<MistakeNoteVO> detail(
            @Parameter(description = "错题ID", required = true) @PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mistakeNoteService.getById(id, userId));
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
}
