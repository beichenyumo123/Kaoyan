package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "OCR识别结果")
public class OCRResultVO {

    @Schema(description = "OCR识别的文本内容，多行以换行符分隔")
    private String text;

    @Schema(description = "传入的原图URL，方便前端回显")
    private String imageUrl;

    @Schema(description = "系统根据文本内容自动推测的科目，可能为null", example = "408计算机")
    private String suggestedSubject;

    @Schema(description = "系统从文本中提取的知识点关键词，多个用逗号分隔，可能为null", example = "操作系统-进程调度")
    private String suggestedKnowledgePoints;
}
