package com.zzu.kaoyan.module.mistake.service;

import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteCreateDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteUpdateDTO;
import com.zzu.kaoyan.module.mistake.entity.vo.*;

import java.time.LocalDate;

public interface MistakeNoteService {

    MistakeNoteVO create(MistakeNoteCreateDTO dto, Long userId);

    MistakeNoteVO update(Long noteId, MistakeNoteUpdateDTO dto, Long userId);

    void delete(Long noteId, Long userId);

    MistakeNoteVO getById(Long noteId, Long userId);

    PageInfo<MistakeNoteVO> page(Integer pageNum, Integer pageSize, String subject, Long userId);

    /**
     * 今日待复习错题列表
     */
    PageInfo<ReviewTaskVO> getTodayReviewTasks(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 完成一道题的复习，更新艾宾浩斯阶段
     */
    ReviewResultVO completeReview(Long noteId, Integer masteryAfter, Integer isCorrect, Long userId);

    /**
     * 统计信息
     */
    MistakeStatsVO getStats(Long userId);

    /**
     * 将 Markdown 文本渲染为 HTML
     */
    String renderMarkdown(String markdown);

    /**
     * 查看错题详情，可选是否渲染 Markdown 为 HTML
     */
    MistakeNoteVO getById(Long noteId, Long userId, boolean renderHtml);

    /**
     * 获取月度日历视图
     */
    CalendarMonthVO getCalendarMonth(Long userId, int year, int month);

    /**
     * 获取指定日期的待复习错题列表
     */
    PageInfo<ReviewTaskVO> getCalendarDayNotes(Long userId, LocalDate date, Integer pageNum, Integer pageSize);

    /**
     * 获取艾宾浩斯遗忘曲线统计
     */
    EbbinghausStatsVO getEbbinghausStats(Long userId, int days);
}
