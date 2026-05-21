package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "某个复习阶段的数据")
public class StageIntervalVO {

    @Schema(description = "阶段号 0-7")
    private Integer stage;

    @Schema(description = "阶段名称")
    private String stageText;

    @Schema(description = "距上次复习间隔（天）")
    private Integer intervalDays;

    @Schema(description = "处于该阶段的题目数")
    private Integer count;

    @Schema(description = "该阶段的平均掌握度")
    private Double avgMastery;
}
