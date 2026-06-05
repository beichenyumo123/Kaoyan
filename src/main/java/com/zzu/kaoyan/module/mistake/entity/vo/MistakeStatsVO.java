package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
@Schema(description = "错题本统计")
public class MistakeStatsVO {

    @Schema(description = "错题总数")
    private Integer totalNotes;

    @Schema(description = "今日待复习数")
    private Integer todayReviewCount;

    @Schema(description = "今日已完成复习数")
    private Integer reviewedToday;

    @Schema(description = "平均掌握度 0-100")
    private Double avgMastery;

    @Schema(description = "科目分布，key=科目名 value=数量", example = "{\"408计算机\":15,\"数学(一)\":10}")
    private Map<String, Integer> subjectDistribution;

    @Schema(description = "艾宾浩斯阶段分布，key=阶段号(0-7) value=数量", example = "{\"0\":5,\"1\":8,\"3\":12,\"7\":3}")
    private Map<Integer, Integer> stageDistribution;
}
