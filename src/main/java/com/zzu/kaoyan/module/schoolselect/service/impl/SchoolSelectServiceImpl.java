package com.zzu.kaoyan.module.schoolselect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.module.schoolselect.entity.dto.RecommendationRequestDTO;
import com.zzu.kaoyan.module.schoolselect.entity.po.RecommendationHistory;
import com.zzu.kaoyan.module.schoolselect.entity.po.SchoolInfo;
import com.zzu.kaoyan.module.schoolselect.entity.vo.RecommendationResultVO;
import com.zzu.kaoyan.module.schoolselect.mapper.RecommendationHistoryMapper;
import com.zzu.kaoyan.module.schoolselect.mapper.SchoolInfoMapper;
import com.zzu.kaoyan.module.schoolselect.service.MatchEngineService;
import com.zzu.kaoyan.module.schoolselect.service.SchoolSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolSelectServiceImpl implements SchoolSelectService {

    private final MatchEngineService matchEngineService;
    private final RecommendationHistoryMapper historyMapper;
    private final SchoolInfoMapper schoolInfoMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecommendationResultVO recommend(RecommendationRequestDTO dto, Long userId) {
        RecommendationResultVO result = matchEngineService.recommend(dto);
        saveHistory(dto, result, userId);
        return result;
    }

    @Override
    public List<RecommendationResultVO> getHistory(Long userId) {
        List<RecommendationHistory> histories = historyMapper.selectList(
            new LambdaQueryWrapper<RecommendationHistory>()
                .eq(RecommendationHistory::getUserId, userId)
                .orderByDesc(RecommendationHistory::getCreatedAt)
                .last("LIMIT 10"));

        List<RecommendationResultVO> result = new ArrayList<>();
        for (RecommendationHistory h : histories) {
            try {
                RecommendationResultVO vo = objectMapper.readValue(
                    h.getResultJson(), RecommendationResultVO.class);
                result.add(vo);
            } catch (JsonProcessingException e) {
                // Skip corrupted history entries
            }
        }
        return result;
    }

    @Override
    public List<SchoolInfo> listSchools(String keyword) {
        LambdaQueryWrapper<SchoolInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SchoolInfo::getName, keyword);
        }
        wrapper.orderByDesc(SchoolInfo::getHotLevel);
        return schoolInfoMapper.selectList(wrapper);
    }

    private void saveHistory(RecommendationRequestDTO dto, RecommendationResultVO result, Long userId) {
        try {
            RecommendationHistory history = new RecommendationHistory();
            history.setUserId(userId);
            history.setInputJson(objectMapper.writeValueAsString(dto));
            history.setResultJson(objectMapper.writeValueAsString(result));
            historyMapper.insert(history);
        } catch (JsonProcessingException e) {
            throw new BusinessException("保存推荐历史失败");
        }
    }
}
