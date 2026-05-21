package com.zzu.kaoyan.module.mistake.service;

import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteCreateDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteUpdateDTO;
import com.zzu.kaoyan.module.mistake.entity.vo.MistakeNoteVO;
import com.zzu.kaoyan.module.mistake.entity.vo.MistakeStatsVO;
import com.zzu.kaoyan.module.mistake.entity.vo.ReviewResultVO;
import com.zzu.kaoyan.module.mistake.entity.vo.ReviewTaskVO;

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
}
