package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "今日待复习任务")
public class ReviewTaskVO {

    @Schema(description = "每日计划ID（用于关联）")
    private Long id;

    @Schema(description = "错题ID（用于完成复习接口）")
    private Long noteId;

    @Schema(description = "科目")
    private String subject;

    @Schema(description = "题目内容")
    private String questionContent;

    @Schema(description = "答案与解析")
    private String answer;

    @Schema(description = "知识点标签")
    private String knowledgePoints;

    @Schema(description = "难度 1-5")
    private Integer difficulty;

    @Schema(description = "当前掌握程度 0-100")
    private Integer masteryLevel;

    @Schema(description = "当前艾宾浩斯复习阶段 0-7")
    private Integer reviewStage;

    @Schema(description = "阶段中文描述")
    private String reviewStageText;

    @Schema(description = "累计复习次数")
    private Integer reviewCount;

    @Schema(description = "今日是否已完成复习")
    private Boolean isCompleted;

    @Schema(description = "计划日期")
    private LocalDate planDate;
}
