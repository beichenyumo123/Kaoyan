package com.zzu.kaoyan.module.mistake.entity.dto;

import com.zzu.kaoyan.common.annotation.SkipXssClean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "从 AI 对话快速收藏错题请求")
public class QuickSaveDTO {

    @NotBlank(message = "学科不能为空")
    @Schema(description = "学科", example = "高等数学")
    private String subject;

    @SkipXssClean  // Markdown 内容，不可经 Jsoup HTML 清洗，否则 \n 全丢
    @NotBlank(message = "题目内容不能为空")
    @Schema(description = "拼接后的题目内容")
    private String questionContent;

    @SkipXssClean  // Markdown 内容，不可经 Jsoup HTML 清洗，否则 \n 全丢
    @Schema(description = "拼接后的解析/答案")
    private String answer;

    @Schema(description = "图片URL（可选）")
    private String imageUrl;

    @NotEmpty(message = "至少选择一条消息")
    @Schema(description = "选中的 AI 对话消息ID列表")
    private List<Long> chatMessageIds;

    @Schema(description = "来源会话ID")
    private Long sessionId;

    @Schema(description = "来源类型", example = "AI_CHAT")
    private String sourceType;

    @Schema(description = "知识点（逗号分隔，可选，不填则自动提取）")
    private String knowledgePoints;
}
