package com.zzu.kaoyan.module.mistake.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteCreateDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteUpdateDTO;
import com.zzu.kaoyan.module.mistake.entity.po.DailyPlanPO;
import com.zzu.kaoyan.module.mistake.entity.po.MistakeNotePO;
import com.zzu.kaoyan.module.mistake.entity.po.ReviewLogPO;
import com.zzu.kaoyan.module.mistake.entity.vo.MistakeNoteVO;
import com.zzu.kaoyan.module.mistake.entity.vo.MistakeStatsVO;
import com.zzu.kaoyan.module.mistake.entity.vo.ReviewResultVO;
import com.zzu.kaoyan.module.mistake.entity.vo.ReviewTaskVO;
import com.zzu.kaoyan.module.mistake.mapper.DailyPlanMapper;
import com.zzu.kaoyan.module.mistake.mapper.MistakeNoteMapper;
import com.zzu.kaoyan.module.mistake.mapper.ReviewLogMapper;
import com.zzu.kaoyan.module.mistake.service.EbbinghausService;
import com.zzu.kaoyan.module.mistake.service.MistakeNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MistakeNoteServiceImpl implements MistakeNoteService {

    private final MistakeNoteMapper mistakeNoteMapper;
    private final ReviewLogMapper reviewLogMapper;
    private final DailyPlanMapper dailyPlanMapper;
    private final EbbinghausService ebbinghausService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MistakeNoteVO create(MistakeNoteCreateDTO dto, Long userId) {
        MistakeNotePO note = new MistakeNotePO();
        BeanUtils.copyProperties(dto, note);
        note.setUserId(userId);
        note.setReviewStage(0);
        note.setReviewCount(0);
        note.setMasteryLevel(0);
        note.setNextReviewDate(LocalDate.now().plusDays(1)); // 明天首次复习
        mistakeNoteMapper.insert(note);
        return toVO(note);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MistakeNoteVO update(Long noteId, MistakeNoteUpdateDTO dto, Long userId) {
        MistakeNotePO note = getOwnedNote(noteId, userId);
        if (dto.getQuestionContent() != null) note.setQuestionContent(dto.getQuestionContent());
        if (dto.getAnswer() != null) note.setAnswer(dto.getAnswer());
        if (dto.getImageUrl() != null) note.setImageUrl(dto.getImageUrl());
        if (dto.getKnowledgePoints() != null) note.setKnowledgePoints(dto.getKnowledgePoints());
        if (dto.getSource() != null) note.setSource(dto.getSource());
        if (dto.getDifficulty() != null) note.setDifficulty(dto.getDifficulty());
        if (dto.getMasteryLevel() != null) note.setMasteryLevel(dto.getMasteryLevel());
        mistakeNoteMapper.updateById(note);
        return toVO(note);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long noteId, Long userId) {
        MistakeNotePO note = getOwnedNote(noteId, userId);
        note.setIsDeleted(1);
        mistakeNoteMapper.updateById(note);
    }

    @Override
    public MistakeNoteVO getById(Long noteId, Long userId) {
        return toVO(getOwnedNote(noteId, userId));
    }

    @Override
    public PageInfo<MistakeNoteVO> page(Integer pageNum, Integer pageSize, String subject, Long userId) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<MistakeNotePO> wrapper = new LambdaQueryWrapper<MistakeNotePO>()
                .eq(MistakeNotePO::getUserId, userId)
                .eq(MistakeNotePO::getIsDeleted, 0)
                .orderByDesc(MistakeNotePO::getCreatedAt);
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq(MistakeNotePO::getSubject, subject);
        }
        List<MistakeNotePO> list = mistakeNoteMapper.selectList(wrapper);
        PageInfo<MistakeNotePO> poPageInfo = new PageInfo<>(list);
        List<MistakeNoteVO> voList = list.stream().map(this::toVO).collect(Collectors.toList());
        PageInfo<MistakeNoteVO> result = new PageInfo<>(voList);
        result.setTotal(poPageInfo.getTotal());
        result.setPageNum(poPageInfo.getPageNum());
        result.setPageSize(poPageInfo.getPageSize());
        return result;
    }

    @Override
    public PageInfo<ReviewTaskVO> getTodayReviewTasks(Long userId, Integer pageNum, Integer pageSize) {
        // 先确保今天的复习计划已生成（文档承诺"手动调用此接口触发生成"）
        ebbinghausService.generateDailyPlan(userId);

        LocalDate today = LocalDate.now();
        DailyPlanPO plan = dailyPlanMapper.selectOne(
                new LambdaQueryWrapper<DailyPlanPO>()
                        .eq(DailyPlanPO::getUserId, userId)
                        .eq(DailyPlanPO::getPlanDate, today)
        );

        if (plan == null || plan.getNoteIds() == null || plan.getNoteIds().isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }

        List<Long> noteIds = plan.getNoteIds();
        Set<Long> completedSet = plan.getCompletedIds() != null
                ? new HashSet<>(plan.getCompletedIds()) : Collections.emptySet();

        // 分页在内存中做
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, noteIds.size());
        if (start >= noteIds.size()) {
            return new PageInfo<>(Collections.emptyList());
        }

        List<Long> pageIds = noteIds.subList(start, end);
        List<MistakeNotePO> notes = mistakeNoteMapper.selectBatchIds(pageIds);

        List<ReviewTaskVO> voList = new ArrayList<>();
        for (MistakeNotePO note : notes) {
            ReviewTaskVO vo = new ReviewTaskVO();
            vo.setId(plan.getId()); // plan id
            vo.setNoteId(note.getId());
            vo.setSubject(note.getSubject());
            vo.setQuestionContent(note.getQuestionContent());
            vo.setAnswer(note.getAnswer());
            vo.setKnowledgePoints(note.getKnowledgePoints());
            vo.setDifficulty(note.getDifficulty());
            vo.setMasteryLevel(note.getMasteryLevel());
            vo.setReviewStage(note.getReviewStage());
            vo.setReviewStageText(ebbinghausService.getStageText(note.getReviewStage()));
            vo.setReviewCount(note.getReviewCount());
            vo.setIsCompleted(completedSet.contains(note.getId()));
            vo.setPlanDate(today);
            voList.add(vo);
        }

        PageInfo<ReviewTaskVO> result = new PageInfo<>(voList);
        result.setTotal(noteIds.size());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResultVO completeReview(Long noteId, Integer masteryAfter, Integer isCorrect, Long userId) {
        MistakeNotePO note = getOwnedNote(noteId, userId);
        int oldStage = note.getReviewStage();
        int oldMastery = note.getMasteryLevel();

        // 记录复习日志
        ReviewLogPO log = new ReviewLogPO();
        log.setNoteId(noteId);
        log.setUserId(userId);
        log.setReviewStage(oldStage);
        log.setMasteryBefore(oldMastery);
        log.setMasteryAfter(masteryAfter);
        log.setIsCorrect(isCorrect);
        log.setReviewedAt(LocalDateTime.now());
        reviewLogMapper.insert(log);

        // 更新错题状态
        int nextStage = ebbinghausService.nextStage(oldStage);
        note.setReviewStage(nextStage);
        note.setMasteryLevel(masteryAfter);
        note.setReviewCount(note.getReviewCount() + 1);
        note.setLastReviewDate(LocalDate.now());
        note.setNextReviewDate(ebbinghausService.calcNextReviewDate(nextStage));
        mistakeNoteMapper.updateById(note);

        // 同步更新每日计划
        LocalDate today = LocalDate.now();
        DailyPlanPO plan = dailyPlanMapper.selectOne(
                new LambdaQueryWrapper<DailyPlanPO>()
                        .eq(DailyPlanPO::getUserId, userId)
                        .eq(DailyPlanPO::getPlanDate, today)
        );
        if (plan != null) {
            List<Long> completedIds = plan.getCompletedIds() != null
                    ? new ArrayList<>(plan.getCompletedIds()) : new ArrayList<>();
            if (!completedIds.contains(noteId)) {
                completedIds.add(noteId);
            }
            plan.setCompletedIds(completedIds);
            plan.setCompletedCount(completedIds.size());
            plan.setIsCompleted(completedIds.size() >= plan.getTotalCount() ? 1 : 0);
            dailyPlanMapper.updateById(plan);
        }

        ReviewResultVO result = new ReviewResultVO();
        result.setNoteId(noteId);
        result.setReviewStage(nextStage);
        result.setReviewStageText(ebbinghausService.getStageText(nextStage));
        result.setMasteryLevel(masteryAfter);
        result.setNextReviewDate(note.getNextReviewDate());
        result.setReviewCount(note.getReviewCount());
        result.setIsCorrect(isCorrect);
        return result;
    }

    @Override
    public MistakeStatsVO getStats(Long userId) {
        // 总错题数
        Long totalNotes = mistakeNoteMapper.selectCount(
                new LambdaQueryWrapper<MistakeNotePO>()
                        .eq(MistakeNotePO::getUserId, userId)
                        .eq(MistakeNotePO::getIsDeleted, 0)
        );

        // 今日复习任务
        LocalDate today = LocalDate.now();
        DailyPlanPO plan = dailyPlanMapper.selectOne(
                new LambdaQueryWrapper<DailyPlanPO>()
                        .eq(DailyPlanPO::getUserId, userId)
                        .eq(DailyPlanPO::getPlanDate, today)
        );
        int todayReviewCount = plan != null && plan.getTotalCount() != null ? plan.getTotalCount() : 0;
        int reviewedToday = plan != null && plan.getCompletedCount() != null ? plan.getCompletedCount() : 0;

        // 平均掌握度
        List<MistakeNotePO> allNotes = mistakeNoteMapper.selectList(
                new LambdaQueryWrapper<MistakeNotePO>()
                        .eq(MistakeNotePO::getUserId, userId)
                        .eq(MistakeNotePO::getIsDeleted, 0)
        );
        double avgMastery = allNotes.stream()
                .mapToInt(n -> n.getMasteryLevel() != null ? n.getMasteryLevel() : 0)
                .average().orElse(0);

        // 科目分布
        Map<String, Integer> subjectDist = allNotes.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getSubject() != null ? n.getSubject() : "未分类",
                        Collectors.summingInt(x -> 1)
                ));

        // 阶段分布
        Map<Integer, Integer> stageDist = allNotes.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getReviewStage() != null ? n.getReviewStage() : 0,
                        Collectors.summingInt(x -> 1)
                ));

        return new MistakeStatsVO(
                totalNotes.intValue(), todayReviewCount, reviewedToday,
                Math.round(avgMastery * 100.0) / 100.0,
                subjectDist, stageDist
        );
    }

    private MistakeNotePO getOwnedNote(Long noteId, Long userId) {
        MistakeNotePO note = mistakeNoteMapper.selectById(noteId);
        if (note == null || note.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "错题不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此错题");
        }
        return note;
    }

    private MistakeNoteVO toVO(MistakeNotePO note) {
        MistakeNoteVO vo = new MistakeNoteVO();
        BeanUtils.copyProperties(note, vo);
        vo.setReviewStageText(ebbinghausService.getStageText(note.getReviewStage()));
        return vo;
    }
}
