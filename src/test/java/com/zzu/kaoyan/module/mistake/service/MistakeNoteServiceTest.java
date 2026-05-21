package com.zzu.kaoyan.module.mistake.service;

import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.module.mistake.entity.po.DailyPlanPO;
import com.zzu.kaoyan.module.mistake.entity.po.MistakeNotePO;
import com.zzu.kaoyan.module.mistake.entity.po.ReviewLogPO;
import com.zzu.kaoyan.module.mistake.entity.vo.ReviewResultVO;
import com.zzu.kaoyan.module.mistake.mapper.DailyPlanMapper;
import com.zzu.kaoyan.module.mistake.mapper.MistakeNoteMapper;
import com.zzu.kaoyan.module.mistake.mapper.ReviewLogMapper;
import com.zzu.kaoyan.module.mistake.service.impl.MistakeNoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 测试 completeReview (完成一道错题的复习) 接口对应的 Service 方法
 * 模拟用户 "liming" (userId=100)
 */
@ExtendWith(MockitoExtension.class)
class MistakeNoteServiceTest {

    @Mock private MistakeNoteMapper mistakeNoteMapper;
    @Mock private ReviewLogMapper reviewLogMapper;
    @Mock private DailyPlanMapper dailyPlanMapper;
    @Mock private EbbinghausService ebbinghausService;

    @InjectMocks
    private MistakeNoteServiceImpl mistakeNoteService;

    /** 模拟用户 liming 的 ID */
    private static final Long LIMING_USER_ID = 100L;
    /** 模拟错题 ID */
    private static final Long NOTE_ID = 1001L;

    private MistakeNotePO sampleNote;

    @BeforeEach
    void setUp() {
        // 构建一个模拟的错题 PO（初始状态：stage=0，mastery=0，reviewCount=0）
        sampleNote = new MistakeNotePO();
        sampleNote.setId(NOTE_ID);
        sampleNote.setUserId(LIMING_USER_ID);
        sampleNote.setSubject("408计算机");
        sampleNote.setQuestionContent("OSI七层模型中，传输层的作用是什么？");
        sampleNote.setAnswer("提供端到端的可靠数据传输");
        sampleNote.setReviewStage(0);
        sampleNote.setMasteryLevel(0);
        sampleNote.setReviewCount(0);
        sampleNote.setNextReviewDate(LocalDate.now().plusDays(1));
        sampleNote.setIsDeleted(0);
    }

    // ==================== 正常场景 ====================

    @Test
    void testCompleteReview_CorrectAnswer_ShouldAdvanceStageAndIncreaseMastery() {
        // liming 的错题在 stage 0，答对了
        when(mistakeNoteMapper.selectById(NOTE_ID)).thenReturn(sampleNote);
        when(ebbinghausService.nextStage(0)).thenReturn(1);
        when(ebbinghausService.calcNextReviewDate(1)).thenReturn(LocalDate.now().plusDays(1));
        when(ebbinghausService.getStageText(1)).thenReturn("第1次复习(1天后)");

        DailyPlanPO plan = createDailyPlan();
        when(dailyPlanMapper.selectOne(any())).thenReturn(plan);

        // 执行
        ReviewResultVO result = mistakeNoteService.completeReview(NOTE_ID, 15, 1, LIMING_USER_ID);

        // 断言返回值
        assertNotNull(result);
        assertEquals(NOTE_ID, result.getNoteId());
        assertEquals(1, result.getReviewStage());
        assertEquals("第1次复习(1天后)", result.getReviewStageText());
        assertEquals(15, result.getMasteryLevel());
        assertEquals(1, result.getReviewCount());
        assertEquals(1, result.getIsCorrect());
        assertNotNull(result.getNextReviewDate());

        // 验证复习日志被保存
        ArgumentCaptor<ReviewLogPO> logCaptor = ArgumentCaptor.forClass(ReviewLogPO.class);
        verify(reviewLogMapper).insert(logCaptor.capture());
        ReviewLogPO savedLog = logCaptor.getValue();
        assertEquals(NOTE_ID, savedLog.getNoteId());
        assertEquals(LIMING_USER_ID, savedLog.getUserId());
        assertEquals(0, savedLog.getReviewStage());       // 旧阶段
        assertEquals(0, savedLog.getMasteryBefore());      // 旧掌握度
        assertEquals(15, savedLog.getMasteryAfter());      // 新掌握度
        assertEquals(1, savedLog.getIsCorrect());

        // 验证错题被更新
        ArgumentCaptor<MistakeNotePO> noteCaptor = ArgumentCaptor.forClass(MistakeNotePO.class);
        verify(mistakeNoteMapper).updateById(noteCaptor.capture());
        MistakeNotePO updatedNote = noteCaptor.getValue();
        assertEquals(1, updatedNote.getReviewStage());
        assertEquals(15, updatedNote.getMasteryLevel());
        assertEquals(1, updatedNote.getReviewCount());
        assertEquals(LocalDate.now(), updatedNote.getLastReviewDate());

        // 验证每日计划被更新
        verify(dailyPlanMapper).updateById(any(DailyPlanPO.class));
    }

    @Test
    void testCompleteReview_WrongAnswer_ShouldStillAdvanceStageButLowerMastery() {
        // liming 的错题，这次答错了
        sampleNote.setReviewStage(2);
        sampleNote.setMasteryLevel(50);
        sampleNote.setReviewCount(3);

        when(mistakeNoteMapper.selectById(NOTE_ID)).thenReturn(sampleNote);
        when(ebbinghausService.nextStage(2)).thenReturn(3);
        when(ebbinghausService.calcNextReviewDate(3)).thenReturn(LocalDate.now().plusDays(3));
        when(ebbinghausService.getStageText(3)).thenReturn("第3次复习(2天后)");

        DailyPlanPO plan = createDailyPlan();
        when(dailyPlanMapper.selectOne(any())).thenReturn(plan);

        // 答错，掌握度从50降到30
        ReviewResultVO result = mistakeNoteService.completeReview(NOTE_ID, 30, 0, LIMING_USER_ID);

        assertNotNull(result);
        assertEquals(3, result.getReviewStage());
        assertEquals("第3次复习(2天后)", result.getReviewStageText());
        assertEquals(30, result.getMasteryLevel());
        assertEquals(4, result.getReviewCount());
        assertEquals(0, result.getIsCorrect());

        // 验证日志中记录了错误
        ArgumentCaptor<ReviewLogPO> logCaptor = ArgumentCaptor.forClass(ReviewLogPO.class);
        verify(reviewLogMapper).insert(logCaptor.capture());
        assertEquals(0, logCaptor.getValue().getIsCorrect());
        assertEquals(50, logCaptor.getValue().getMasteryBefore());
        assertEquals(30, logCaptor.getValue().getMasteryAfter());
    }

    @Test
    void testCompleteReview_NoDailyPlan_ShouldNotFail() {
        // 如果没有今天的每日计划，不应抛异常
        when(mistakeNoteMapper.selectById(NOTE_ID)).thenReturn(sampleNote);
        when(ebbinghausService.nextStage(0)).thenReturn(1);
        when(ebbinghausService.calcNextReviewDate(1)).thenReturn(LocalDate.now().plusDays(1));
        when(ebbinghausService.getStageText(1)).thenReturn("第1次复习(1天后)");
        when(dailyPlanMapper.selectOne(any())).thenReturn(null); // 没有计划

        ReviewResultVO result = mistakeNoteService.completeReview(NOTE_ID, 20, 1, LIMING_USER_ID);

        assertNotNull(result);
        assertEquals(1, result.getReviewStage());
        // 不应调用 updateById on dailyPlanMapper
        verify(dailyPlanMapper, never()).updateById(any(DailyPlanPO.class));
    }

    // ==================== 异常场景 ====================

    @Test
    void testCompleteReview_NoteNotFound_ShouldThrowException() {
        when(mistakeNoteMapper.selectById(NOTE_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            mistakeNoteService.completeReview(NOTE_ID, 15, 1, LIMING_USER_ID);
        });
        assertEquals("错题不存在", ex.getMessage());
    }

    @Test
    void testCompleteReview_NoteBelongsToOtherUser_ShouldThrowForbidden() {
        // 错题属于另一个用户（userId=999），不是 liming
        sampleNote.setUserId(999L);
        when(mistakeNoteMapper.selectById(NOTE_ID)).thenReturn(sampleNote);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            mistakeNoteService.completeReview(NOTE_ID, 15, 1, LIMING_USER_ID);
        });
        assertEquals("无权操作此错题", ex.getMessage());
    }

    @Test
    void testCompleteReview_DeletedNote_ShouldThrowException() {
        sampleNote.setIsDeleted(1);
        when(mistakeNoteMapper.selectById(NOTE_ID)).thenReturn(sampleNote);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            mistakeNoteService.completeReview(NOTE_ID, 15, 1, LIMING_USER_ID);
        });
        assertEquals("错题不存在", ex.getMessage());
    }

    // ==================== 边界场景 ====================

    @Test
    void testCompleteReview_MaxStageReached_ShouldReturnNullNextReviewDate() {
        // stage 7 → 8 (已掌握)
        sampleNote.setReviewStage(7);
        sampleNote.setMasteryLevel(85);
        sampleNote.setReviewCount(7);

        when(mistakeNoteMapper.selectById(NOTE_ID)).thenReturn(sampleNote);
        when(ebbinghausService.nextStage(7)).thenReturn(8);
        when(ebbinghausService.calcNextReviewDate(8)).thenReturn(null); // 已掌握，无需再复习
        when(ebbinghausService.getStageText(8)).thenReturn("已掌握");

        DailyPlanPO plan = createDailyPlan();
        when(dailyPlanMapper.selectOne(any())).thenReturn(plan);

        ReviewResultVO result = mistakeNoteService.completeReview(NOTE_ID, 100, 1, LIMING_USER_ID);

        assertNotNull(result);
        assertEquals(8, result.getReviewStage());
        assertEquals("已掌握", result.getReviewStageText());
        assertNull(result.getNextReviewDate());
        assertEquals(8, result.getReviewCount());
    }

    // ==================== 辅助方法 ====================

    private DailyPlanPO createDailyPlan() {
        DailyPlanPO plan = new DailyPlanPO();
        plan.setId(5001L);
        plan.setUserId(LIMING_USER_ID);
        plan.setPlanDate(LocalDate.now());
        plan.setNoteIds(new ArrayList<>(Arrays.asList(NOTE_ID, 1002L, 1003L)));
        plan.setCompletedIds(new ArrayList<>());
        plan.setTotalCount(3);
        plan.setCompletedCount(0);
        plan.setIsCompleted(0);
        return plan;
    }
}
