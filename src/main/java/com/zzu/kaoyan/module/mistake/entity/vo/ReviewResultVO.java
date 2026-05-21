package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "完成复习结果")
public class ReviewResultVO {

    @Schema(description = "错题ID")
    private Long noteId;

    @Schema(description = "更新后的艾宾浩斯复习阶段 0-7")
    private Integer reviewStage;

    @Schema(description = "阶段中文描述")
    private String reviewStageText;

    @Schema(description = "更新后的掌握程度 0-100")
    private Integer masteryLevel;

    @Schema(description = "下次复习日期")
    private LocalDate nextReviewDate;

    @Schema(description = "累计复习次数")
    private Integer reviewCount;

    @Schema(description = "本次是否答对 1=对 0=错")
    private Integer isCorrect;
}
