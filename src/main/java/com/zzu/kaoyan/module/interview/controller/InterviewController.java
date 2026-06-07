package com.zzu.kaoyan.module.interview.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interview.entity.InterviewRecord;
import com.zzu.kaoyan.module.interview.entity.InterviewSession;
import com.zzu.kaoyan.module.interview.entity.vo.ReportVO;
import com.zzu.kaoyan.module.interview.mapper.InterviewSessionMapper;
import com.zzu.kaoyan.module.interview.service.InterviewAiService;
import com.zzu.kaoyan.module.interview.service.InterviewReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 模拟复试官 - 控制器
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Tag(name = "AI模拟复试官")
public class InterviewController {

    private final InterviewAiService interviewAiService;
    private final InterviewReportService interviewReportService;
    private final com.zzu.kaoyan.module.interview.service.InterviewTtsService interviewTtsService;
    private final InterviewSessionMapper sessionMapper;
    private final com.zzu.kaoyan.module.interview.mapper.InterviewRecordMapper recordMapper;

    // ============================================================
    // 会话管理
    // ============================================================

    /**
     * 创建新的面试会话
     */
    @PostMapping("/session/create")
    @Operation(summary = "创建面试会话")
    public Result<InterviewSession> createSession(@RequestBody CreateSessionDTO dto) {
        // 尝试从登录态获取用户ID，未登录时使用请求中的 userId 或默认值 1
        Long userId = getCurrentUserId();
        if (userId == null && dto.getUserId() != null) {
            userId = dto.getUserId();
        }
        if (userId == null) {
            userId = 1L; // 开发/测试阶段的默认用户ID
        }

        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setTargetSchool(dto.getTargetSchool());
        session.setTargetMajor(dto.getTargetMajor());
        session.setInterviewType(dto.getInterviewType());
        session.setStatus("IN_PROGRESS");
        sessionMapper.insert(session);
        return Result.success(session);
    }

    /**
     * 获取会话基本信息
     */
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "获取面试会话信息")
    public Result<InterviewSession> getSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return Result.error(404, "会话不存在");
        }
        return Result.success(session);
    }

    // ============================================================
    // AI 对话
    // ============================================================

    /**
     * 获取历史对话记录
     */
    @GetMapping("/session/{sessionId}/records")
    @Operation(summary = "获取会话的历史对话记录")
    public Result<List<InterviewRecord>> getRecords(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        List<InterviewRecord> records = recordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InterviewRecord>()
                        .eq(InterviewRecord::getSessionId, sessionId)
                        .orderByAsc(InterviewRecord::getCreatedAt)
        );
        return Result.success(records);
    }

    /**
     * 发送用户回答，获取 AI 追问
     */
    @PostMapping("/session/{sessionId}/next-question")
    @Operation(summary = "发送回答并获取AI追问")
    public Result<InterviewRecord> nextQuestion(
            @Parameter(description = "会话ID") @PathVariable Long sessionId,
            @RequestBody NextQuestionDTO dto) {
        InterviewRecord aiRecord = interviewAiService.generateNextQuestion(
                sessionId, dto.getAnswer(), dto.getSpeechDuration(), dto.getDemeanor());
        return Result.success(aiRecord);
    }

    // ============================================================
    // 报告生成
    // ============================================================

    /**
     * 结束面试并生成评估报告
     */
    @PostMapping("/session/{sessionId}/finish")
    @Operation(summary = "结束面试并生成报告")
    public Result<ReportVO> finishInterview(
            @Parameter(description = "会话ID") @PathVariable Long sessionId,
            @RequestBody(required = false) FinishDTO dto) {
        java.util.Map<String, Object> demeanorSummary = dto != null ? dto.getDemeanorSummary() : null;
        ReportVO report = interviewReportService.generateFinalReport(sessionId, demeanorSummary);
        return Result.success(report);
    }

    // ============================================================
    // TTS 语音合成
    // ============================================================

    /**
     * 将 AI 面试官的文本回复合成为 MP3 语音
     */
    @PostMapping("/tts")
    @Operation(summary = "TTS语音合成")
    public ResponseEntity<byte[]> synthesizeSpeech(
            @RequestBody TtsRequestDTO dto) {
        String interviewType = null;
        if (dto.getSessionId() != null) {
            var session = sessionMapper.selectById(dto.getSessionId());
            if (session != null) {
                interviewType = session.getInterviewType();
            }
        }
        byte[] audioBytes = interviewTtsService.synthesize(dto.getText(), interviewType);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType("audio/mpeg"))
                .body(audioBytes);
    }

    // ============================================================
    // 内部 DTO
    // ============================================================

    @lombok.Data
    public static class CreateSessionDTO {
        private Long userId;          // 可选，未登录时手动指定
        private String targetSchool;
        private String targetMajor;
        private String interviewType;
    }

    // ============================================================
    // 私有辅助方法
    // ============================================================

    /**
     * 安全获取当前登录用户ID，未登录时返回 null
     */
    private Long getCurrentUserId() {
        try {
            return cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    @lombok.Data
    public static class NextQuestionDTO {
        private String answer;
        /** 语音回答的实际时长（秒），文字模式为 null */
        private Double speechDuration;
        /** 视频模式的仪态快照，非视频模式为 null */
        private java.util.Map<String, Object> demeanor;
    }

    @lombok.Data
    public static class FinishDTO {
        /** 视频模式下的仪态汇总数据，非视频模式为 null */
        private java.util.Map<String, Object> demeanorSummary;
    }

    @lombok.Data
    public static class TtsRequestDTO {
        /** 可选，传入后可自动选择中文/英文语音 */
        private Long sessionId;
        /** 待合成的文本 */
        private String text;
    }
}
