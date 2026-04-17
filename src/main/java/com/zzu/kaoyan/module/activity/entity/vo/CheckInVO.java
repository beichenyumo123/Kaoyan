package com.zzu.kaoyan.module.activity.entity.vo;

import lombok.Data;

@Data
public class CheckInVO {
    private Boolean todayChecked;
    private Integer continuousDays;
    private Integer points;
    private Integer totalPoints;
    private Integer totalCheckDays;
}