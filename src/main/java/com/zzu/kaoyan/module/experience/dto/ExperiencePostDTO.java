package com.zzu.kaoyan.module.experience.dto;

import com.zzu.kaoyan.module.experience.entity.ExperiencePost;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "创建/编辑经验贴请求体")
public class ExperiencePostDTO {

    @Schema(description = "关联论坛帖子ID（可选）")
    private Long forumPostId;

    @NotBlank(message = "本科院校不能为空")
    @Schema(description = "本科院校", example = "郑州大学")
    private String undergradSchool;

    @NotBlank(message = "本科专业不能为空")
    @Schema(description = "本科专业", example = "软件工程")
    private String undergradMajor;

    @NotNull(message = "请选择是否跨考")
    @Schema(description = "是否跨考")
    private Boolean isCrossMajor;

    @NotNull(message = "请选择是否二战")
    @Schema(description = "是否二战")
    private Boolean isSecondAttempt;

    @NotBlank(message = "目标院校不能为空")
    @Schema(description = "目标院校", example = "清华大学")
    private String targetSchool;

    @NotBlank(message = "目标专业不能为空")
    @Schema(description = "目标专业", example = "计算机技术")
    private String targetMajor;

    @Schema(description = "初试总分", example = "385.0")
    private BigDecimal initialExamTotal;

    @Schema(description = "政治", example = "72.0")
    private BigDecimal initialExamPolitics;

    @Schema(description = "英语", example = "78.0")
    private BigDecimal initialExamEnglish;

    @Schema(description = "数学", example = "125.0")
    private BigDecimal initialExamMath;

    @Schema(description = "专业课", example = "110.0")
    private BigDecimal initialExamMajor;

    @Schema(description = "复试分", example = "88.5")
    private BigDecimal reExamScore;

    @Schema(description = "备考时间线")
    private List<ExperiencePost.TimelineItem> timelineJson;

    @Schema(description = "用书推荐")
    private List<ExperiencePost.BookItem> booksJson;

    @Schema(description = "备考心得")
    private String tips;
}