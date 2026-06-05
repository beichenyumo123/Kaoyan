package com.zzu.kaoyan.module.schoolselect.service;

import com.zzu.kaoyan.module.schoolselect.entity.dto.RecommendationRequestDTO;
import com.zzu.kaoyan.module.schoolselect.entity.po.SchoolInfo;
import com.zzu.kaoyan.module.schoolselect.entity.vo.RecommendationResultVO;
import java.util.List;

public interface SchoolSelectService {

    RecommendationResultVO recommend(RecommendationRequestDTO dto, Long userId);

    List<RecommendationResultVO> getHistory(Long userId);

    List<SchoolInfo> listSchools(String keyword);
}
