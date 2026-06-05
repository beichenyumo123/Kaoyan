package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日复习准确率")
public class DailyAccuracyVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "当日复习总题数")
    private Integer totalReviewed;

    @Schema(description = "当日答对题数")
    private Integer correct;
}
