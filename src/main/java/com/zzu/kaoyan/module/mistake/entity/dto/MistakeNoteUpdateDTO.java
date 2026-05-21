package com.zzu.kaoyan.module.mistake.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新错题请求（传什么改什么）")
public class MistakeNoteUpdateDTO {

    @NotNull(message = "错题ID不能为空")
    @Schema(description = "错题ID", required = true)
    private Long id;

    @Schema(description = "题目内容")
    private String questionContent;

    @Schema(description = "答案与解析")
    private String answer;

    @Schema(description = "原题图片URL")
    private String imageUrl;

    @Schema(description = "知识点标签，多个用逗号分隔")
    private String knowledgePoints;

    @Schema(description = "题目来源")
    private String source;

    @Schema(description = "难度 1-5")
    private Integer difficulty;

    @Schema(description = "手动修正掌握度 0-100")
    private Integer masteryLevel;
}
