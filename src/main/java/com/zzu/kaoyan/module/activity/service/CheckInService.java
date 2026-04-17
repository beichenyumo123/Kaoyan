package com.zzu.kaoyan.module.activity.service;

import com.zzu.kaoyan.module.activity.entity.dto.CheckInDTO;
import com.zzu.kaoyan.module.activity.entity.vo.CheckInVO;

public interface CheckInService {
    CheckInVO checkIn(CheckInDTO dto, Long userId);
    Boolean isCheckedToday(Long userId);
    CheckInVO getUserStats(Long userId);
}