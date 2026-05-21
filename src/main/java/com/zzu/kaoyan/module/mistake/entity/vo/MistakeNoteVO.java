package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "错题详情")
public class MistakeNoteVO {

    @Schema(description = "错题ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "科目")
    private String subject;

    @Schema(description = "题目内容")
    private String questionContent;

    @Schema(description = "答案与解析")
    private String answer;

    @Schema(description = "原题图片URL")
    private String imageUrl;

    @Schema(description = "知识点标签，逗号分隔")
    private String knowledgePoints;

    @Schema(description = "题目来源")
    private String source;

    @Schema(description = "难度 1-5")
    private Integer difficulty;

    @Schema(description = "掌握程度 0-100")
    private Integer masteryLevel;

    @Schema(description = "当前艾宾浩斯复习阶段 0-7")
    private Integer reviewStage;

    @Schema(description = "累计复习次数")
    private Integer reviewCount;

    @Schema(description = "下次复习日期，null表示已掌握")
    private LocalDate nextReviewDate;

    @Schema(description = "上次复习日期，null表示还未复习过")
    private LocalDate lastReviewDate;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "阶段中文描述", example = "第3次复习(2天后)")
    private String reviewStageText;
}
