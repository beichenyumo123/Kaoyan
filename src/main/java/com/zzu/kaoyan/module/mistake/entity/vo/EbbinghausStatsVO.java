package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "艾宾浩斯遗忘曲线统计数据")
public class EbbinghausStatsVO {

    @Schema(description = "各阶段的数据分布（用于遗忘曲线柱状图）")
    private List<StageIntervalVO> stageDistribution = new ArrayList<>();

    @Schema(description = "最近N天的复习准确率趋势")
    private List<DailyAccuracyVO> dailyAccuracyTrend = new ArrayList<>();

    @Schema(description = "掌握度分布（0-20, 21-40, ...）")
    private List<MasteryDistributionVO> masteryDistribution = new ArrayList<>();
}
