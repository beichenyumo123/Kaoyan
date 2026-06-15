package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "日历中某一天的数据")
public class CalendarDayVO {

    @Schema(description = "日期（几号）")
    private int day;

    @Schema(description = "当日待复习总数")
    private int count;

    @Schema(description = "当日已完成数")
    private int completedCount;

    @Schema(description = "是否为今天")
    private boolean isToday;
}
