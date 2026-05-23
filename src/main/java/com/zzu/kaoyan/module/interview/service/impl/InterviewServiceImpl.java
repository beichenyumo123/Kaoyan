package com.zzu.kaoyan.module.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.interview.entity.dto.StartInterviewDTO;
import com.zzu.kaoyan.module.interview.entity.dto.SubmitAnswerDTO;
import com.zzu.kaoyan.module.interview.entity.po.InterviewQuestion;
import com.zzu.kaoyan.module.interview.entity.po.InterviewReport;
import com.zzu.kaoyan.module.interview.entity.po.InterviewSession;
import com.zzu.kaoyan.module.interview.entity.vo.*;
import com.zzu.kaoyan.module.interview.mapper.InterviewQuestionMapper;
import com.zzu.kaoyan.module.interview.mapper.InterviewReportMapper;
import com.zzu.kaoyan.module.interview.mapper.InterviewSessionMapper;
import com.zzu.kaoyan.module.interview.service.AIInterviewService;
import com.zzu.kaoyan.module.interview.service.InterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InterviewServiceImpl implements InterviewService {

    private static final int MAX_QUESTIONS = 8;

    private final InterviewSessionMapper sessionMapper;
    private final InterviewQuestionMapper questionMapper;
    private final InterviewReportMapper reportMapper;
    private final UserMapper userMapper;
    private final AIInterviewService aiService;

    public InterviewServiceImpl(InterviewSessionMapper sessionMapper,
                                InterviewQuestionMapper questionMapper,
                                InterviewReportMapper reportMapper,
                                UserMapper userMapper,
                                AIInterviewService aiService) {
        this.sessionMapper = sessionMapper;
        this.questionMapper = questionMapper;
        this.reportMapper = reportMapper;
        this.userMapper = userMapper;
        this.aiService = aiService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO startInterview(Long userId, StartInterviewDTO dto) {
        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setSchoolStyle(dto.getSchoolStyle());
        session.setMajor(dto.getMajor());
        session.setInterviewType(dto.getInterviewType());
        session.setStatus(0);
        session.setTotalQuestions(0);
        session.setAnsweredQuestions(0);
        session.setStartedAt(LocalDateTime.now());
        sessionMapper.insert(session);

        return generateNextQuestion(session, "");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO submitAnswer(Long userId, SubmitAnswerDTO dto) {
        InterviewSession session = sessionMapper.selectById(dto.getSessionId());
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "面试会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此会话");
        }
        if (session.getStatus() == 1) {
            throw new BusinessException(400, "该面试已结束");
        }

        InterviewQuestion currentQuestion = questionMapper.selectOne(
                new LambdaQueryWrapper<InterviewQuestion>()
                        .eq(InterviewQuestion::getSessionId, session.getId())
                        .eq(InterviewQuestion::getQuestionNumber, session.getAnsweredQuestions() + 1)
                        .eq(InterviewQuestion::getIsDeleted, 0));
        if (currentQuestion == null) {
            throw new BusinessException(400, "当前没有问题待回答");
        }
        if (currentQuestion.getUserAnswer() != null && !currentQuestion.getUserAnswer().isEmpty()) {
            throw new BusinessException(400, "当前问题已提交过回答");
        }

        // AI评分
        currentQuestion.setUserAnswer(dto.getAnswer());
        aiService.evaluateAnswer(currentQuestion, dto.getAnswer(), session.getSchoolStyle());
        questionMapper.updateById(currentQuestion);

        session.setAnsweredQuestions(session.getAnsweredQuestions() + 1);
        sessionMapper.updateById(session);

        if (session.getAnsweredQuestions() >= MAX_QUESTIONS) {
            endInterviewInternal(session);
            return null;
        }

        String previousQAs = buildPreviousQAs(session.getId());
        return generateNextQuestion(session, previousQAs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewReportVO endInterview(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "面试会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此会话");
        }
        if (session.getStatus() == 1) {
            InterviewReport existingReport = reportMapper.selectOne(
                    new LambdaQueryWrapper<InterviewReport>()
                            .eq(InterviewReport::getSessionId, sessionId)
                            .eq(InterviewReport::getIsDeleted, 0));
            if (existingReport != null) {
                return toReportVO(existingReport);
            }
        }
        return endInterviewInternal(session);
    }

    @Override
    public InterviewSessionVO getSession(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1 || !session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "面试会话不存在");
        }
        return toSessionVO(session);
    }

    @Override
    public List<InterviewSessionVO> getHistory(Long userId, int pageNum, int pageSize) {
        Page<InterviewSession> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InterviewSession> wrapper = new LambdaQueryWrapper<InterviewSession>()
                .eq(InterviewSession::getUserId, userId)
                .eq(InterviewSession::getIsDeleted, 0)
                .orderByDesc(InterviewSession::getCreatedAt);
        Page<InterviewSession> result = sessionMapper.selectPage(page, wrapper);
        if (result.getRecords().isEmpty()) return Collections.emptyList();
        return result.getRecords().stream().map(this::toSessionVO).collect(Collectors.toList());
    }

    @Override
    public InterviewReportVO getReport(Long userId, Long sessionId) {
        InterviewReport report = reportMapper.selectOne(
                new LambdaQueryWrapper<InterviewReport>()
                        .eq(InterviewReport::getSessionId, sessionId)
                        .eq(InterviewReport::getIsDeleted, 0));
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "评估报告不存在，请先结束面试");
        }
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看此报告");
        }
        return toReportVO(report);
    }

    @Override
    public List<QuestionVO> getSessionQuestions(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1 || !session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "面试会话不存在");
        }
        List<InterviewQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<InterviewQuestion>()
                        .eq(InterviewQuestion::getSessionId, sessionId)
                        .eq(InterviewQuestion::getIsDeleted, 0)
                        .orderByAsc(InterviewQuestion::getQuestionNumber));
        return questions.stream().map(this::toQuestionVO).collect(Collectors.toList());
    }

    // ============ private helpers ============

    private QuestionVO generateNextQuestion(InterviewSession session, String previousQAs) {
        int nextNum = session.getTotalQuestions() + 1;
        String questionText = aiService.generateQuestion(
                session.getSchoolStyle(), session.getMajor(),
                session.getInterviewType(), nextNum, previousQAs);

        String qType = mapQuestionType(session.getInterviewType());

        InterviewQuestion question = new InterviewQuestion();
        question.setSessionId(session.getId());
        question.setQuestionNumber(nextNum);
        question.setQuestionContent(questionText);
        question.setQuestionType(qType);
        questionMapper.insert(question);

        session.setTotalQuestions(nextNum);
        sessionMapper.updateById(session);

        return toQuestionVO(question);
    }

    private InterviewReportVO endInterviewInternal(InterviewSession session) {
        session.setStatus(1);
        session.setEndedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        List<InterviewQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<InterviewQuestion>()
                        .eq(InterviewQuestion::getSessionId, session.getId())
                        .eq(InterviewQuestion::getIsDeleted, 0)
                        .orderByAsc(InterviewQuestion::getQuestionNumber));

        String allQAs = buildAllQAs(questions);
        String summary = aiService.generateSummaryReport(
                session.getSchoolStyle(), session.getMajor(), session.getInterviewType(), allQAs);

        InterviewReport report = new InterviewReport();
        report.setSessionId(session.getId());
        report.setUserId(session.getUserId());

        int[] scores = calculateFinalScores(questions);
        report.setTotalScore(scores[0]);
        report.setContentDepthScore(scores[1]);
        report.setLanguageExpressScore(scores[2]);
        report.setPsychologyScore(scores[3]);
        report.setComprehensiveScore(scores[4]);
        report.setSummary(summary);
        report.setStrengths(buildStrengths(scores));
        report.setWeaknesses(buildWeaknesses(scores));
        report.setImprovementAdvice(buildAdvice(scores, session));
        report.setSuggestedPractice(buildPractice(session));
        reportMapper.insert(report);

        return toReportVO(report);
    }

    private int[] calculateFinalScores(List<InterviewQuestion> questions) {
        if (questions.isEmpty()) return new int[]{50, 50, 50, 50, 50};

        int total = 0, contentDepth = 0, languageExpress = 0, psychology = 0, comprehensive = 0;
        int count = 0;
        for (InterviewQuestion q : questions) {
            if (q.getAiScore() != null) {
                total += q.getAiScore();
                count++;
            }
            if (q.getDimensionScores() != null) {
                DimensionScoresVO d = parseDimensionScores(q.getDimensionScores());
                contentDepth += d.getContentDepth();
                languageExpress += d.getLanguageExpress();
                psychology += d.getPsychology();
                comprehensive += (d.getLogicClarity() + d.getProfessionalKnowledge() + d.getAdaptability()) / 3;
            }
        }
        if (count == 0) count = 1;
        return new int[]{
                total / count,
                contentDepth / questions.size(),
                languageExpress / questions.size(),
                psychology / questions.size(),
                comprehensive / questions.size()
        };
    }

    private String buildStrengths(int[] scores) {
        if (scores[1] >= 70) return "专业知识掌握较为扎实，能够对核心概念进行准确定义和阐述。";
        return "基本能够回答面试官的问题，展现出一定的专业基础。";
    }

    private String buildWeaknesses(int[] scores) {
        StringBuilder sb = new StringBuilder();
        if (scores[1] < 60) sb.append("专业知识深度有待加强，建议夯实基础理论；");
        if (scores[2] < 60) sb.append("语言组织和表达需要更加清晰流畅；");
        if (scores[3] < 60) sb.append("面对压力时表现略显紧张，建议加强心理素质训练；");
        if (sb.isEmpty()) sb.append("在个别问题的回答上可以更加深入和具体。");
        return sb.toString();
    }

    private String buildAdvice(int[] scores, InterviewSession session) {
        StringBuilder sb = new StringBuilder();
        if (scores[1] < 65) sb.append("① 系统复习专业课核心知识点，重点理解概念之间的关联；");
        if (scores[2] < 65) sb.append("② 练习用「总-分-总」结构组织回答，先说结论再展开论述；");
        if (scores[3] < 65) sb.append("③ 进行多次模拟练习，适应面试节奏和压力场景；");
        if (session.getInterviewType().contains("english")) {
            sb.append("④ 加强英语口语练习，每天坚持15分钟英语自我表达；");
        }
        if (sb.isEmpty()) sb.append("继续保持当前水平，注重细节打磨和前沿知识的补充。");
        return sb.toString();
    }

    private String buildPractice(InterviewSession session) {
        return String.format("建议每天进行30分钟的%s专项练习，并针对%s院校的面试风格做针对性准备。"
                        + "可结合历年真题和学长学姐经验贴进行系统训练。",
                getTypeDisplayName(session.getInterviewType()),
                getSchoolDisplayName(session.getSchoolStyle()));
    }

    private String buildPreviousQAs(Long sessionId) {
        List<InterviewQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<InterviewQuestion>()
                        .eq(InterviewQuestion::getSessionId, sessionId)
                        .eq(InterviewQuestion::getIsDeleted, 0)
                        .orderByAsc(InterviewQuestion::getQuestionNumber));
        return buildAllQAs(questions);
    }

    private String buildAllQAs(List<InterviewQuestion> questions) {
        StringBuilder sb = new StringBuilder();
        for (InterviewQuestion q : questions) {
            sb.append("Q").append(q.getQuestionNumber()).append(": ")
                    .append(q.getQuestionContent()).append("\n");
            if (q.getUserAnswer() != null) {
                sb.append("A").append(q.getQuestionNumber()).append(": ")
                        .append(q.getUserAnswer()).append("\n");
            }
        }
        return sb.toString();
    }

    private String mapQuestionType(String interviewType) {
        return switch (interviewType) {
            case "english_self", "english_qa" -> "english";
            case "professional" -> "professional";
            case "stress" -> "stress";
            default -> "behavioral";
        };
    }

    private InterviewSessionVO toSessionVO(InterviewSession s) {
        InterviewSessionVO vo = new InterviewSessionVO();
        vo.setId(s.getId());
        vo.setUserId(s.getUserId());
        vo.setSchoolStyle(s.getSchoolStyle());
        vo.setSchoolStyleName(getSchoolDisplayName(s.getSchoolStyle()));
        vo.setMajor(s.getMajor());
        vo.setInterviewType(s.getInterviewType());
        vo.setInterviewTypeName(getTypeDisplayName(s.getInterviewType()));
        vo.setStatus(s.getStatus());
        vo.setTotalQuestions(s.getTotalQuestions());
        vo.setAnsweredQuestions(s.getAnsweredQuestions());
        vo.setStartedAt(s.getStartedAt());
        vo.setEndedAt(s.getEndedAt());
        return vo;
    }

    private QuestionVO toQuestionVO(InterviewQuestion q) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setSessionId(q.getSessionId());
        vo.setQuestionNumber(q.getQuestionNumber());
        vo.setQuestionContent(q.getQuestionContent());
        vo.setQuestionType(q.getQuestionType());
        vo.setUserAnswer(q.getUserAnswer());
        vo.setAiScore(q.getAiScore());
        vo.setAiComment(q.getAiComment());
        if (q.getDimensionScores() != null) {
            vo.setDimensionScores(parseDimensionScores(q.getDimensionScores()));
        }
        return vo;
    }

    private InterviewReportVO toReportVO(InterviewReport r) {
        InterviewReportVO vo = new InterviewReportVO();
        vo.setId(r.getId());
        vo.setSessionId(r.getSessionId());
        vo.setUserId(r.getUserId());
        vo.setTotalScore(r.getTotalScore());
        vo.setContentDepthScore(r.getContentDepthScore());
        vo.setLanguageExpressScore(r.getLanguageExpressScore());
        vo.setPsychologyScore(r.getPsychologyScore());
        vo.setComprehensiveScore(r.getComprehensiveScore());
        vo.setSummary(r.getSummary());
        vo.setStrengths(r.getStrengths());
        vo.setWeaknesses(r.getWeaknesses());
        vo.setImprovementAdvice(r.getImprovementAdvice());
        vo.setSuggestedPractice(r.getSuggestedPractice());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }

    private DimensionScoresVO parseDimensionScores(String json) {
        DimensionScoresVO vo = new DimensionScoresVO();
        try {
            String content = json.replace("{", "").replace("}", "").replace("\"", "");
            for (String part : content.split(",")) {
                String[] kv = part.split(":");
                if (kv.length != 2) continue;
                int val = Integer.parseInt(kv[1].trim());
                switch (kv[0].trim()) {
                    case "contentDepth" -> vo.setContentDepth(val);
                    case "logicClarity" -> vo.setLogicClarity(val);
                    case "languageExpress" -> vo.setLanguageExpress(val);
                    case "professionalKnowledge" -> vo.setProfessionalKnowledge(val);
                    case "adaptability" -> vo.setAdaptability(val);
                    case "psychology" -> vo.setPsychology(val);
                }
            }
        } catch (Exception e) {
            log.warn("解析维度评分JSON失败: {}", json);
            vo.setContentDepth(60);
            vo.setLogicClarity(60);
            vo.setLanguageExpress(60);
            vo.setProfessionalKnowledge(60);
            vo.setAdaptability(60);
            vo.setPsychology(60);
        }
        return vo;
    }

    private String getSchoolDisplayName(String style) {
        return switch (style) {
            case "tsinghua" -> "清华大学";
            case "tongji" -> "同济大学";
            case "985" -> "985院校";
            case "211" -> "211院校";
            default -> "其他院校";
        };
    }

    private String getTypeDisplayName(String type) {
        return switch (type) {
            case "english_self" -> "英语自我介绍";
            case "english_qa" -> "英语问答";
            case "professional" -> "专业课面试";
            case "stress" -> "压力面试";
            case "comprehensive" -> "综合面试";
            default -> type;
        };
    }
}
