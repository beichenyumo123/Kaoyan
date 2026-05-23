package com.zzu.kaoyan.module.interview.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interview.entity.dto.StartInterviewDTO;
import com.zzu.kaoyan.module.interview.entity.dto.SubmitAnswerDTO;
import com.zzu.kaoyan.module.interview.entity.vo.InterviewReportVO;
import com.zzu.kaoyan.module.interview.entity.vo.InterviewSessionVO;
import com.zzu.kaoyan.module.interview.entity.vo.QuestionVO;
import com.zzu.kaoyan.module.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@Tag(name = "AI模拟复试官", description = "模拟复试面试流程：开始面试、回答问题、获取评估报告")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @Operation(summary = "开始模拟复试", description = "指定院校风格、专业和面试类型，创建复试会话并返回第一个问题")
    @PostMapping("/start")
    @SaCheckLogin
    public Result<QuestionVO> startInterview(@Valid @RequestBody StartInterviewDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(interviewService.startInterview(userId, dto));
    }

    @Operation(summary = "提交回答", description = "提交当前问题的回答，获取AI评分和下一个问题。如果返回data为null表示面试已完成")
    @PostMapping("/answer")
    @SaCheckLogin
    public Result<QuestionVO> submitAnswer(@Valid @RequestBody SubmitAnswerDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        QuestionVO next = interviewService.submitAnswer(userId, dto);
        return Result.success(next);
    }

    @Operation(summary = "结束面试", description = "主动结束面试并生成多维评估报告")
    @PostMapping("/end/{sessionId}")
    @SaCheckLogin
    public Result<InterviewReportVO> endInterview(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(interviewService.endInterview(userId, sessionId));
    }

    @Operation(summary = "获取面试会话详情", description = "查看某个面试会话的基本信息")
    @GetMapping("/session/{sessionId}")
    @SaCheckLogin
    public Result<InterviewSessionVO> getSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(interviewService.getSession(userId, sessionId));
    }

    @Operation(summary = "获取面试历史记录", description = "分页获取当前用户的模拟复试历史")
    @GetMapping("/history")
    @SaCheckLogin
    public Result<List<InterviewSessionVO>> getHistory(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(interviewService.getHistory(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取评估报告", description = "查看面试后的多维评估报告")
    @GetMapping("/report/{sessionId}")
    @SaCheckLogin
    public Result<InterviewReportVO> getReport(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(interviewService.getReport(userId, sessionId));
    }

    @Operation(summary = "获取面试问答详情", description = "查看某次面试的全部问答记录（含评分）")
    @GetMapping("/questions/{sessionId}")
    @SaCheckLogin
    public Result<List<QuestionVO>> getSessionQuestions(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(interviewService.getSessionQuestions(userId, sessionId));
    }
}
