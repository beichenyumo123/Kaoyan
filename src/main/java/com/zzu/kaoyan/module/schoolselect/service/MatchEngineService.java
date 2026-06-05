package com.zzu.kaoyan.module.schoolselect.service;

import com.zzu.kaoyan.module.schoolselect.entity.dto.RecommendationRequestDTO;
import com.zzu.kaoyan.module.schoolselect.entity.vo.RecommendationResultVO;

public interface MatchEngineService {

    RecommendationResultVO recommend(RecommendationRequestDTO dto);
}
