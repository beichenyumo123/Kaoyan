package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "月度日历视图")
public class CalendarMonthVO {

    @Schema(description = "年份")
    private int year;

    @Schema(description = "月份 (1-12)")
    private int month;

    @Schema(description = "当月各天数据")
    private List<CalendarDayVO> days;
}
