package com.zzu.kaoyan.module.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.activity.entity.dto.CheckInDTO;
import com.zzu.kaoyan.module.activity.entity.po.CheckInPO;
import com.zzu.kaoyan.module.activity.entity.po.PointsLogPO;
import com.zzu.kaoyan.module.activity.entity.po.UserStudyPO;
import com.zzu.kaoyan.module.activity.entity.vo.CheckInVO;
import com.zzu.kaoyan.module.activity.mapper.CheckInMapper;
import com.zzu.kaoyan.module.activity.mapper.PointsLogMapper;
import com.zzu.kaoyan.module.activity.mapper.UserStudyMapper;
import com.zzu.kaoyan.module.activity.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final CheckInMapper checkInMapper;
    private final UserStudyMapper userStudyMapper;
    private final PointsLogMapper pointsLogMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckInVO checkIn(CheckInDTO dto, Long userId) {
        LocalDate today = LocalDate.now();

        boolean exists = checkInMapper.existsByUserAndDate(userId, today);
        if (exists) {
            throw new BusinessException("今日已打卡");
        }

        UserStudyPO study = userStudyMapper.selectByUserId(userId);
        int continuousDays = 1;

        if (study != null && study.getLastCheckDate() != null) {
            if (study.getLastCheckDate().plusDays(1).equals(today)) {
                continuousDays = study.getContinuousDays() + 1;
            }
        }

        CheckInPO po = new CheckInPO();
        po.setUserId(userId);
        po.setStudyHours(dto.getStudyHours());
        po.setNotes(dto.getNotes());
        po.setCreatedDate(today);
        po.setContinuousDays(continuousDays);
        checkInMapper.insert(po);

        updateStudy(userId, continuousDays, today);

        int points = givePoints(userId, continuousDays);

        CheckInVO vo = new CheckInVO();
        vo.setTodayChecked(true);
        vo.setContinuousDays(continuousDays);
        vo.setPoints(points);
        vo.setTotalPoints(userMapper.getPointsById(userId));
        vo.setTotalCheckDays(study == null ? 1 : study.getTotalCheckDays() + 1);
        return vo;
    }

    private void updateStudy(Long userId, int continuousDays, LocalDate today) {
        UserStudyPO po = userStudyMapper.selectByUserId(userId);
        if (po == null) {
            po = new UserStudyPO();
            po.setUserId(userId);
            po.setContinuousDays(continuousDays);
            po.setTotalCheckDays(1);
            po.setLastCheckDate(today);
            userStudyMapper.insert(po);
        } else {
            po.setContinuousDays(continuousDays);
            po.setTotalCheckDays(po.getTotalCheckDays() + 1);
            po.setLastCheckDate(today);
            userStudyMapper.updateById(po);
        }
    }

    private int givePoints(Long userId, int continuousDays) {
        int base = 3;
        if (continuousDays == 3) base += 2;
        if (continuousDays == 7) base += 5;
        if (continuousDays == 30) base += 20;

        userMapper.addPoints(userId, base);

        PointsLogPO log = new PointsLogPO();
        log.setUserId(userId);
        log.setPoints(base);
        log.setType("CHECK_IN");
        log.setDescription("打卡+" + base + "分");
        pointsLogMapper.insert(log);
        return base;
    }

    @Override
    public Boolean isCheckedToday(Long userId) {
        return checkInMapper.existsByUserAndDate(userId, LocalDate.now());
    }

    @Override
    public CheckInVO getUserStats(Long userId) {
        CheckInVO vo = new CheckInVO();
        vo.setTodayChecked(isCheckedToday(userId));

        UserStudyPO study = userStudyMapper.selectByUserId(userId);
        if (study != null) {
            vo.setContinuousDays(study.getContinuousDays());
            vo.setTotalCheckDays(study.getTotalCheckDays());
        } else {
            vo.setContinuousDays(0);
            vo.setTotalCheckDays(0);
        }

        vo.setTotalPoints(userMapper.getPointsById(userId));
        return vo;
    }
}